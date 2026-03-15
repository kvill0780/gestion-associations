package bf.kvill.associa.system.association;

import bf.kvill.associa.system.association.dto.*;
import bf.kvill.associa.members.user.dto.MemberSummaryDto;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.dto.ApiResponse;
import bf.kvill.associa.shared.dto.PageResponse;
import bf.kvill.associa.system.association.mapper.AssociationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des associations
 * 
 * Endpoints :
 * - GET /api/system/associations - Liste associations
 * - GET /api/system/associations/{id} - Détails association
 * - GET /api/system/associations/slug/{slug} - Par slug
 * - POST /api/system/associations - Créer association
 * - PUT /api/system/associations/{id} - Modifier association
 * - DELETE /api/system/associations/{id} - Supprimer association
 * - POST /api/system/associations/{id}/suspend - Suspendre
 * - POST /api/system/associations/{id}/activate - Activer
 * - POST /api/system/associations/{id}/archive - Archiver
 * - GET /api/system/associations/{id}/stats - Statistiques
 */
@RestController
@RequestMapping("/api/system/associations")
@RequiredArgsConstructor
@Tag(name = "Associations", description = "Gestion des associations — CRUD, statuts et bureau exécutif")
@SecurityRequirement(name = "bearerAuth")
public class AssociationController {

    private final AssociationService associationService;
    private final AssociationMapper associationMapper;
    private final UserRepository userRepository;

    /**
     * Récupère une association par son ID
     */
    @Operation(summary = "Récupérer une association par ID")
    @GetMapping("/{id}")
    public ResponseEntity<AssociationResponseDto> getAssociation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ensureAssociationAccess(principal, id);
        Association association = associationService.findById(id);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupère une association par son slug
     */
    @Operation(summary = "Récupérer une association par slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<AssociationResponseDto> getAssociationBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Association association = associationService.findBySlug(slug);
        ensureAssociationAccess(principal, association.getId());
        AssociationResponseDto dto = associationMapper.toResponseDto(association);
        return ResponseEntity.ok(dto);
    }

    /**
     * Liste toutes les associations
     * 
     * Query params :
     * - type : Filtrer par type (STUDENT, PROFESSIONAL, etc.)
     * - status : Filtrer par statut (ACTIVE, INACTIVE, etc.)
     */
    @Operation(summary = "Lister toutes les associations", description = "Filtres optionnels : type et status")
    @GetMapping
    public ResponseEntity<List<AssociationSummaryDto>> getAllAssociations(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (!isSuperAdmin(principal)) {
            Association ownAssociation = associationService.findById(principal.getAssociationId());
            if (!matchesFilters(ownAssociation, type, status)) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(List.of(associationMapper.toSummaryDto(ownAssociation)));
        }

        List<Association> associations;

        if (type != null && status != null) {
            associations = associationService.findByTypeAndStatus(type, status);
        } else if (type != null) {
            associations = associationService.findByType(type);
        } else if (status != null) {
            associations = associationService.findByStatus(status);
        } else {
            associations = associationService.findAll();
        }

        List<AssociationSummaryDto> dtos = associations.stream()
                .map(associationMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Liste paginée des associations
     */
    @Operation(summary = "Liste paginée des associations")
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<AssociationSummaryDto>> getAssociationsPaginated(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (!isSuperAdmin(principal)) {
            Association ownAssociation = associationService.findById(principal.getAssociationId());
            List<AssociationSummaryDto> content = matchesFilters(ownAssociation, null, status)
                    ? List.of(associationMapper.toSummaryDto(ownAssociation))
                    : List.of();

            PageResponse<AssociationSummaryDto> response = PageResponse.<AssociationSummaryDto>builder()
                    .content(content)
                    .pageNumber(0)
                    .pageSize(pageable.getPageSize())
                    .totalElements(content.size())
                    .totalPages(content.isEmpty() ? 0 : 1)
                    .first(true)
                    .last(true)
                    .build();

            return ResponseEntity.ok(response);
        }

        Page<Association> page = status != null
                ? associationService.findByStatusPaginated(status, pageable)
                : associationService.findAllPaginated(pageable);

        List<AssociationSummaryDto> content = page.getContent().stream()
                .map(associationMapper::toSummaryDto)
                .collect(Collectors.toList());

        PageResponse<AssociationSummaryDto> response = PageResponse.<AssociationSummaryDto>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Créer une nouvelle association
     * 
     * Action sensible : crée automatiquement les rôles templates
     * 
     * Permissions : SUPER_ADMIN uniquement
     */
    @Operation(summary = "Créer une association", description = "SUPER_ADMIN uniquement — crée automatiquement les rôles templates")
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AssociationResponseDto>> createAssociation(
            @Valid @RequestBody CreateAssociationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Charger l'utilisateur depuis le principal
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Association association = associationService.createAssociation(request, currentUser);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Association créée avec succès", dto));
    }

    /**
     * Modifier une association
     * 
     * Permissions : SUPER_ADMIN ou PRESIDENT de l'association
     */
    @Operation(summary = "Modifier une association", description = "SUPER_ADMIN ou Président de l'association")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or " +
            "(hasRole('PRESIDENT') and @associationSecurityService.canManage(#id, authentication.principal.id))")
    public ResponseEntity<ApiResponse<AssociationResponseDto>> updateAssociation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssociationRequest request) {
        Association association = associationService.updateAssociation(id, request);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);

        return ResponseEntity.ok(ApiResponse.success("Association mise à jour", dto));
    }

    /**
     * Supprimer une association (soft delete)
     * 
     * Action critique : supprime toutes les données liées
     * 
     * Permissions : SUPER_ADMIN uniquement
     */
    @Operation(summary = "Supprimer une association", description = "SUPER_ADMIN uniquement — soft delete")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAssociation(@PathVariable Long id) {
        associationService.deleteAssociation(id);
        return ResponseEntity.ok(ApiResponse.success("Association supprimée", null));
    }

    /**
     * Suspendre une association
     * 
     * Les membres ne peuvent plus se connecter
     * Les opérations sont bloquées
     * 
     * Permissions : SUPER_ADMIN uniquement
     */
    @Operation(summary = "Suspendre une association", description = "Bloque les connexions des membres")
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AssociationResponseDto>> suspendAssociation(@PathVariable Long id) {
        Association association = associationService.suspendAssociation(id);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);

        return ResponseEntity.ok(ApiResponse.success("Association suspendue", dto));
    }

    /**
     * Activer une association
     * 
     * Permissions : SUPER_ADMIN uniquement
     */
    @Operation(summary = "Activer une association")
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AssociationResponseDto>> activateAssociation(@PathVariable Long id) {
        Association association = associationService.activateAssociation(id);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);

        return ResponseEntity.ok(ApiResponse.success("Association activée", dto));
    }

    /**
     * Archiver une association
     * 
     * L'association devient en lecture seule
     * Utile pour les associations dissoutes
     * 
     * Permissions : SUPER_ADMIN uniquement
     */
    @Operation(summary = "Archiver une association", description = "Passage en lecture seule pour les associations dissoutes")
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AssociationResponseDto>> archiveAssociation(@PathVariable Long id) {
        Association association = associationService.archiveAssociation(id);
        AssociationResponseDto dto = associationMapper.toResponseDto(association);

        return ResponseEntity.ok(ApiResponse.success("Association archivée", dto));
    }

    /**
     * Statistiques d'une association
     */
    @Operation(summary = "Statistiques d'une association")
    @GetMapping("/{id}/stats")
    public ResponseEntity<AssociationStatsDto> getAssociationStats(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ensureAssociationAccess(principal, id);
        AssociationStatsDto stats = associationService.getAssociationStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère le bureau exécutif actuel
     */
    @Operation(summary = "Bureau exécutif actuel")
    @GetMapping("/{id}/executive-board")
    public ResponseEntity<List<ExecutiveBoardMemberDto>> getExecutiveBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ensureAssociationAccess(principal, id);
        List<ExecutiveBoardMemberDto> board = associationService.getExecutiveBoard(id);
        return ResponseEntity.ok(board);
    }

    /**
     * Récupère les membres actifs de l'association
     */
    @Operation(summary = "Membres actifs de l'association")
    @GetMapping("/{id}/active-members")
    public ResponseEntity<List<MemberSummaryDto>> getActiveMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ensureAssociationAccess(principal, id);
        List<MemberSummaryDto> members = associationService.getActiveMembers(id);
        return ResponseEntity.ok(members);
    }

    /**
     * Recherche d'associations par nom
     */
    @Operation(summary = "Recherche par nom")
    @GetMapping("/search")
    public ResponseEntity<List<AssociationSummaryDto>> searchAssociations(
            @RequestParam String query,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (!isSuperAdmin(principal)) {
            Association ownAssociation = associationService.findById(principal.getAssociationId());
            String normalizedQuery = query != null ? query.toLowerCase() : "";
            boolean match = ownAssociation.getName() != null
                    && ownAssociation.getName().toLowerCase().contains(normalizedQuery);
            if (!match) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(List.of(associationMapper.toSummaryDto(ownAssociation)));
        }

        List<Association> associations = associationService.searchByName(query);

        List<AssociationSummaryDto> dtos = associations.stream()
                .map(associationMapper::toSummaryDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Vérifier la disponibilité d'un slug
     */
    @Operation(summary = "Vérifier disponibilité d'un slug")
    @GetMapping("/check-slug/{slug}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SlugAvailabilityDto> checkSlugAvailability(@PathVariable String slug) {
        boolean available = !associationService.existsBySlug(slug);

        SlugAvailabilityDto dto = SlugAvailabilityDto.builder()
                .slug(slug)
                .available(available)
                .build();

        return ResponseEntity.ok(dto);
    }

    private void ensureAssociationAccess(CustomUserPrincipal principal, Long associationId) {
        if (isSuperAdmin(principal)) {
            return;
        }
        if (principal.getAssociationId() == null || !principal.getAssociationId().equals(associationId)) {
            throw new AccessDeniedException("Accès interdit hors de votre association");
        }
    }

    private boolean isSuperAdmin(CustomUserPrincipal principal) {
        if (principal == null || principal.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : principal.getAuthorities()) {
            if (authority != null && "ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesFilters(Association association, String type, String status) {
        if (association == null) {
            return false;
        }
        boolean typeMatch = type == null
                || (association.getType() != null && association.getType().name().equalsIgnoreCase(type));
        boolean statusMatch = status == null
                || (association.getStatus() != null && association.getStatus().name().equalsIgnoreCase(status));
        return typeMatch && statusMatch;
    }
}
