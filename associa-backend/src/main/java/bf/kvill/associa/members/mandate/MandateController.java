package bf.kvill.associa.members.mandate;

import bf.kvill.associa.members.mandate.dto.AssignPostRequest;
import bf.kvill.associa.members.mandate.dto.MandateResponseDto;
import bf.kvill.associa.members.mandate.dto.RevokeMandateRequest;
import bf.kvill.associa.members.mandate.mapper.MandateMapper;
import bf.kvill.associa.shared.dto.ApiResponse;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members/mandates")
@RequiredArgsConstructor
@Tag(name = "Mandats", description = "Attribution de postes, révocation et prolongation de mandats")
@SecurityRequirement(name = "bearerAuth")
public class MandateController {

        private final MandateService mandateService;
        private final MandateMapper mandateMapper;

        @Operation(summary = "Récupérer un mandat par ID")
        @GetMapping("/{id}")
        public ResponseEntity<MandateResponseDto> getMandate(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Mandate mandate = mandateService.findById(id);
                ensureAssociationAccess(principal, extractAssociationId(mandate));
                return ResponseEntity.ok((mandateMapper.toResponseDto(mandate)));
        }

        @Operation(summary = "Mandats d'un utilisateur", description = "activeOnly=true pour ne récupérer que les mandats en cours")
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<MandateResponseDto>> getUserMandates(
                        @PathVariable Long userId,
                        @RequestParam(defaultValue = "false") boolean activeOnly,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                List<Mandate> mandates = activeOnly
                                ? mandateService.findActiveUserMandates(userId)
                                : mandateService.findAllUserMandates(userId);

                mandates.forEach(m -> ensureAssociationAccess(principal, extractAssociationId(m)));

                List<MandateResponseDto> dtos = mandates.stream()
                                .map(mandateMapper::toResponseDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        @Operation(summary = "Mandats actifs d'une association")
        @GetMapping("/association/{associationId}/current")
        public ResponseEntity<List<MandateResponseDto>> getCurrentMandates(
                        @PathVariable Long associationId,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                ensureAssociationAccess(principal, associationId);
                List<Mandate> mandates = mandateService.findCurrentMandates(associationId);

                List<MandateResponseDto> dtos = mandates.stream()
                                .map(mandateMapper::toResponseDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        /**
         * ENDPOINT CLÉ : Attribution d'un poste
         *
         * Workflow atomique :
         * 1. Désactive ancien mandat
         * 2. Crée nouveau mandat
         * 3. Attribue rôle (optionnel)
         * 4. Log audit
         */
        @Operation(summary = "Attribuer un poste", description = "Workflow atomique : désactive ancien mandat, crée nouveau mandat, attribue rôle optionnel, log audit")
        @PostMapping("/assign-post")
        @PreAuthorize("hasPermission(null, 'posts.manage')")
        @Transactional
        public ResponseEntity<ApiResponse<MandateResponseDto>> assignPost(
                        @Valid @RequestBody AssignPostRequest request,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {

                Mandate mandate = mandateService.assignPost(
                                request.getUserId(),
                                request.getPostId(),
                                request.getStartDate(),
                                request.getEndDate(),
                                principal.getId(),
                                request.getAssignRole(),
                                request.getRoleOverrideId(),
                                request.getNotes());

                MandateResponseDto dto = mandateMapper.toResponseDto(mandate);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Poste attribué avec succès", dto));
        }

        @Operation(summary = "Révoquer un mandat")
        @PostMapping("/{id}/revoke")
        @PreAuthorize("hasPermission(null, 'posts.manage')")
        public ResponseEntity<ApiResponse<MandateResponseDto>> revokeMandate(
                        @PathVariable Long id,
                        @Valid @RequestBody RevokeMandateRequest request,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Mandate mandate = mandateService.revokeMandate(id, request, principal.getId());
                MandateResponseDto dto = mandateMapper.toResponseDto(mandate);

                return ResponseEntity.ok(ApiResponse.success("Mandat révoqué", dto));
        }

        @Operation(summary = "Prolonger un mandat", description = "Étend la date de fin du mandat")
        @PostMapping("/{id}/extend")
        @PreAuthorize("hasPermission(null, 'posts.manage')")
        public ResponseEntity<ApiResponse<MandateResponseDto>> extendMandate(
                        @PathVariable Long id,
                        @RequestParam LocalDate newEndDate,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Mandate mandate = mandateService.extendMandate(id, newEndDate, principal.getId());
                MandateResponseDto dto = mandateMapper.toResponseDto(mandate);

                return ResponseEntity.ok(ApiResponse.success("Mandat prolongé", dto));
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

        private Long extractAssociationId(Mandate mandate) {
                return mandate != null && mandate.getAssociation() != null ? mandate.getAssociation().getId() : null;
        }
}
