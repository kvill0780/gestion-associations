package bf.kvill.associa.members.post;

import bf.kvill.associa.members.post.dto.*;
import bf.kvill.associa.members.post.mapper.PostMapper;
import bf.kvill.associa.members.role.dto.RoleSummaryDto;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.dto.ApiResponse;
import bf.kvill.associa.shared.dto.PageResponse;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des postes
 *
 * Endpoints :
 * - GET    /api/members/posts              - Liste tous les postes
 * - GET    /api/members/posts/{id}         - Détails d'un poste
 * - POST   /api/members/posts              - Créer un poste
 * - PUT    /api/members/posts/{id}         - Modifier un poste
 * - DELETE /api/members/posts/{id}         - Supprimer un poste
 * - POST   /api/members/posts/{id}/deactivate - Désactiver un poste
 * - POST   /api/members/posts/{id}/activate   - Activer un poste
 * - GET    /api/members/posts/{id}/current-holders - Titulaires actuels
 */
@RestController
@RequestMapping("/api/members/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    /**
     * Récupère un poste par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post post = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(post));
        PostResponseDto dto = postMapper.toResponseDto(post);
        return ResponseEntity.ok(dto);
    }

    /**
     * Liste tous les postes (avec filtres optionnels)
     *
     * Query params :
     * - associationId : Filtrer par association
     * - executiveOnly : Afficher seulement le bureau exécutif
     * - activeOnly : Afficher seulement les postes actifs
     */
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts(
            @RequestParam(required = false) Long associationId,
            @RequestParam(required = false, defaultValue = "false") Boolean executiveOnly,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long effectiveAssociationId = associationId;
        if (!isSuperAdmin(principal)) {
            effectiveAssociationId = principal.getAssociationId();
            if (associationId != null && !Objects.equals(associationId, effectiveAssociationId)) {
                throw new AccessDeniedException("Accès interdit hors de votre association");
            }
        }

        List<Post> posts;

        if (effectiveAssociationId != null && executiveOnly) {
            posts = postService.findExecutivePostsByAssociation(effectiveAssociationId);
        } else if (effectiveAssociationId != null) {
            posts = postService.findByAssociation(effectiveAssociationId);
        } else if (executiveOnly) {
            posts = postService.findAllExecutivePosts();
        } else {
            posts = postService.findAll();
        }

        // Filtre actif/inactif
        if (activeOnly) {
            posts = posts.stream()
                    .filter(Post::isActive)
                    .collect(Collectors.toList());
        }

        List<PostResponseDto> dtos = posts.stream()
                .map(postMapper::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Liste paginée des postes
     */
    @GetMapping("/paginated")
    public ResponseEntity<PageResponse<PostResponseDto>> getPostsPaginated(
            @RequestParam(required = false) Long associationId,
            @PageableDefault(size = 20, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long effectiveAssociationId = associationId;
        if (!isSuperAdmin(principal)) {
            effectiveAssociationId = principal.getAssociationId();
            if (associationId != null && !Objects.equals(associationId, effectiveAssociationId)) {
                throw new AccessDeniedException("Accès interdit hors de votre association");
            }
        }

        Page<Post> page = effectiveAssociationId != null
                ? postService.findByAssociationPaginated(effectiveAssociationId, pageable)
                : postService.findAllPaginated(pageable);

        List<PostResponseDto> content = page.getContent().stream()
                .map(postMapper::toResponseDto)
                .collect(Collectors.toList());

        PageResponse<PostResponseDto> response = PageResponse.<PostResponseDto>builder()
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
     * Créer un nouveau poste
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @PostMapping
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<PostResponseDto>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (!isSuperAdmin(principal)) {
            request.setAssociationId(principal.getAssociationId());
        }
        Post post = postService.createPost(request);
        PostResponseDto dto = postMapper.toResponseDto(post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Poste créé avec succès", dto));
    }

    /**
     * Modifier un poste
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<PostResponseDto>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        Post post = postService.updatePost(id, request);
        PostResponseDto dto = postMapper.toResponseDto(post);

        return ResponseEntity.ok(ApiResponse.success("Poste mis à jour", dto));
    }

    /**
     * Supprimer un poste
     *
     * Impossible si des mandats actifs existent
     *
     * Permissions : SUPER_ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success("Poste supprimé", null));
    }

    /**
     * Désactiver un poste (soft delete)
     *
     * Le poste existe toujours mais ne peut plus être attribué
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<PostResponseDto>> deactivatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        Post post = postService.deactivatePost(id);
        PostResponseDto dto = postMapper.toResponseDto(post);

        return ResponseEntity.ok(ApiResponse.success("Poste désactivé", dto));
    }

    /**
     * Activer un poste
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<PostResponseDto>> activatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        Post post = postService.activatePost(id);
        PostResponseDto dto = postMapper.toResponseDto(post);

        return ResponseEntity.ok(ApiResponse.success("Poste activé", dto));
    }

    /**
     * Récupère les titulaires actuels d'un poste
     */
    @GetMapping("/{id}/current-holders")
    public ResponseEntity<List<PostHolderDto>> getCurrentHolders(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        List<PostHolderDto> holders = postService.getCurrentHolders(id);
        return ResponseEntity.ok(holders);
    }

    /**
     * Lier un rôle à un poste (suggestion automatique)
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @PostMapping("/{postId}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<Void>> linkRoleToPost(
            @PathVariable Long postId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(postId);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        postService.linkRoleToPost(postId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Rôle lié au poste", null));
    }

    /**
     * Délier un rôle d'un poste
     *
     * Permissions : SUPER_ADMIN, PRESIDENT
     */
    @DeleteMapping("/{postId}/roles/{roleId}")
    @PreAuthorize("hasPermission(null, 'posts.manage')")
    public ResponseEntity<ApiResponse<Void>> unlinkRoleFromPost(
            @PathVariable Long postId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(postId);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        postService.unlinkRoleFromPost(postId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Rôle délié du poste", null));
    }

    /**
     * Récupère les rôles suggérés pour un poste
     */
    @GetMapping("/{id}/suggested-roles")
    public ResponseEntity<List<RoleSummaryDto>> getSuggestedRoles(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        List<RoleSummaryDto> roles = postService.getSuggestedRoles(id);
        return ResponseEntity.ok(roles);
    }

    /**
     * Statistiques d'un poste
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<PostStatsDto> getPostStats(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Post existing = postService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));

        PostStatsDto stats = postService.getPostStats(id);
        return ResponseEntity.ok(stats);
    }

    private void ensureAssociationAccess(CustomUserPrincipal principal, Long associationId) {
        if (isSuperAdmin(principal)) {
            return;
        }
        if (principal == null || principal.getAssociationId() == null || !principal.getAssociationId().equals(associationId)) {
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

    private Long extractAssociationId(Post post) {
        return post != null && post.getAssociation() != null ? post.getAssociation().getId() : null;
    }
}
