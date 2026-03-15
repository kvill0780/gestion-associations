package bf.kvill.associa.members.role;

import bf.kvill.associa.members.role.dto.AssignRoleRequest;
import bf.kvill.associa.members.role.dto.CreateRoleRequest;
import bf.kvill.associa.members.role.dto.RoleResponseDto;
import bf.kvill.associa.members.role.dto.UpdateRoleRequest;
import bf.kvill.associa.members.role.mapper.RoleMapper;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * Contrôleur pour la gestion des rôles
 * Permet de créer, modifier, supprimer et consulter les rôles d'une association
 */
@RestController
@RequestMapping("/api/members/roles")
@RequiredArgsConstructor
@Tag(name = "Rôles", description = "Gestion des rôles et leur attribution aux membres")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

        private final RoleService roleService;
        private final RoleMapper roleMapper;

        /**
         * Récupère un rôle par son ID
         */
        @Operation(summary = "Récupérer un rôle par ID")
        @GetMapping("/{id}")
        public ResponseEntity<RoleResponseDto> getRole(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Role role = roleService.findById(id);
                ensureAssociationAccess(principal, role.getAssociation().getId());
                return ResponseEntity.ok(roleMapper.toResponseDto(role));
        }

        /**
         * Récupère tous les rôles d'une association
         */
        @Operation(summary = "Tous les rôles", description = "Paramètre optionnel associationId pour filtrer")
        @GetMapping
        public ResponseEntity<List<RoleResponseDto>> getAllRoles(
                        @RequestParam(required = false) Long associationId,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Long effectiveAssociationId = associationId;
                if (!isSuperAdmin(principal)) {
                        effectiveAssociationId = principal.getAssociationId();
                        if (associationId != null && !Objects.equals(associationId, effectiveAssociationId)) {
                                throw new AccessDeniedException("Accès interdit hors de votre association");
                        }
                }

                List<Role> roles = effectiveAssociationId != null
                                ? roleService.findByAssociation(effectiveAssociationId)
                                : roleService.findAll();

                List<RoleResponseDto> dtos = roles.stream()
                                .map(roleMapper::toResponseDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        @Operation(summary = "Rôles d'une association")
        @GetMapping("/association/{associationId}")
        public ResponseEntity<List<RoleResponseDto>> getRolesByAssociation(
                        @PathVariable Long associationId,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                ensureAssociationAccess(principal, associationId);
                List<Role> roles = roleService.findByAssociation(associationId);

                List<RoleResponseDto> dtos = roles.stream()
                                .map(roleMapper::toResponseDto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        /**
         * Crée un nouveau rôle
         */
        @Operation(summary = "Créer un rôle", description = "Requiert la permission roles.manage")
        @PostMapping
        @PreAuthorize("hasPermission(null, 'roles.manage')")
        public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(
                        @Valid @RequestBody CreateRoleRequest request,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                if (!isSuperAdmin(principal)) {
                        request.setAssociationId(principal.getAssociationId());
                }

                Role role = roleService.createRole(request, principal.getId());
                RoleResponseDto dto = roleMapper.toResponseDto(role);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Rôle créé avec succès", dto));
        }

        /**
         * Met à jour un rôle existant
         */
        @Operation(summary = "Modifier un rôle", description = "Requiert la permission roles.manage")
        @PutMapping("/{id}")
        @PreAuthorize("hasPermission(null, 'roles.manage')")
        public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateRoleRequest request,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                Role role = roleService.updateRole(id, request, principal.getId());
                RoleResponseDto dto = roleMapper.toResponseDto(role);

                return ResponseEntity.ok(ApiResponse.success("Rôle mis à jour", dto));
        }

        /**
         * Supprime un rôle
         */
        @Operation(summary = "Supprimer un rôle", description = "Réservé SUPER_ADMIN")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteRole(
                        @PathVariable Long id,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                roleService.deleteRole(id, principal.getId());
                return ResponseEntity.ok(ApiResponse.success("Rôle supprimé", null));
        }

        @Operation(summary = "Attribuer un rôle à un membre")
        @PostMapping("/assign")
        @PreAuthorize("hasPermission(null, 'roles.manage')")
        public ResponseEntity<ApiResponse<Void>> assignRole(
                        @Valid @RequestBody AssignRoleRequest request,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                roleService.assignRoleToUser(
                                request.getUserId(),
                                request.getRoleId(),
                                principal.getId(),
                                request.getTermStart(),
                                request.getTermEnd());

                return ResponseEntity.ok(ApiResponse.success("Rôle attribué", null));
        }

        @Operation(summary = "Révoquer un rôle d'un membre")
        @PostMapping("/revoke")
        @PreAuthorize("hasPermission(null, 'roles.manage')")
        public ResponseEntity<ApiResponse<Void>> revokeRole(
                        @RequestParam Long userId,
                        @RequestParam Long roleId,
                        @AuthenticationPrincipal CustomUserPrincipal principal) {
                roleService.revokeRoleFromUser(userId, roleId, principal.getId());
                return ResponseEntity.ok(ApiResponse.success("Rôle révoqué", null));
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
}
