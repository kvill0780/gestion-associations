package bf.kvill.associa.members.user;

import bf.kvill.associa.members.user.dto.*;
import bf.kvill.associa.members.user.mapper.UserMapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members/users")
@RequiredArgsConstructor
@Tag(name = "Membres", description = "Gestion des membres d'une association")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Récupérer un membre par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'members.view')")
    public ResponseEntity<MemberResponseDto> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User user = userService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(user));
        return ResponseEntity.ok(userMapper.toResponseDto(user));
    }

    @Operation(summary = "Membres d'une association", description = "Filtre par associationId")
    @GetMapping("/association/{associationId}")
    @PreAuthorize("hasPermission(null, 'members.view')")
    public ResponseEntity<List<MemberSummaryDto>> getUsersByAssociation(
            @PathVariable Long associationId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ensureAssociationAccess(principal, associationId);
        List<User> users = userService.findByAssociation(associationId);
        return ResponseEntity.ok(users.stream().map(userMapper::toSummaryDto).collect(Collectors.toList()));
    }

    @Operation(summary = "Tous les membres", description = "Paramètre optionnel associationId pour filtrer")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'members.view')")
    public ResponseEntity<List<MemberResponseDto>> getAllUsers(
            @RequestParam(required = false) Long associationId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long effectiveAssociationId = associationId;
        if (!isSuperAdmin(principal)) {
            effectiveAssociationId = principal.getAssociationId();
            if (associationId != null && !associationId.equals(effectiveAssociationId)) {
                throw new AccessDeniedException("Accès interdit hors de votre association");
            }
        }

        List<User> users = effectiveAssociationId != null
                ? userService.findByAssociation(effectiveAssociationId)
                : userService.findAll();

        List<MemberResponseDto> dtos = users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Créer un membre")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'members.create')")
    public ResponseEntity<ApiResponse<MemberResponseDto>> createUser(
            @Valid @RequestBody CreateMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (!isSuperAdmin(principal)) {
            request.setAssociationId(principal.getAssociationId());
        }
        User user = userService.createUser(request);
        MemberResponseDto dto = userMapper.toResponseDto(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Membre créé avec succès", dto));
    }

    @Operation(summary = "Modifier un membre")
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'members.update')")
    public ResponseEntity<ApiResponse<MemberResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User existing = userService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));
        User user = userService.updateUser(id, request);
        MemberResponseDto dto = userMapper.toResponseDto(user);

        return ResponseEntity.ok(ApiResponse.success("Membre mis à jour", dto));
    }

    @Operation(summary = "Activer l'adhésion", description = "Passe le statut du membre à ACTIVE")
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasPermission(null, 'members.update')")
    public ResponseEntity<ApiResponse<MemberResponseDto>> activateMembership(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User existing = userService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));
        User updated = userService.activateMembership(id);
        MemberResponseDto dto = userMapper.toResponseDto(updated);
        return ResponseEntity.ok(ApiResponse.success("Membre activé", dto));
    }

    @Operation(summary = "Approuver un membre en attente")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasPermission(null, 'members.approve')")
    public ResponseEntity<ApiResponse<MemberResponseDto>> approveMember(
            @PathVariable Long id,
            @Valid @RequestBody ApproveMemberRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User existing = userService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));
        User user = userService.approveMember(id, request);
        MemberResponseDto dto = userMapper.toResponseDto(user);

        return ResponseEntity.ok(ApiResponse.success("Membre approuvé", dto));
    }

    @Operation(summary = "Suspendre un membre")
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasPermission(null, 'members.update')")
    public ResponseEntity<ApiResponse<MemberResponseDto>> suspendUser(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User existing = userService.findById(id);
        ensureAssociationAccess(principal, extractAssociationId(existing));
        User updated = userService.suspendUser(id);
        MemberResponseDto dto = userMapper.toResponseDto(updated);
        return ResponseEntity.ok(ApiResponse.success("Membre suspendu", dto));
    }

    @Operation(summary = "Supprimer un membre", description = "Soft delete — réservé SUPER_ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Membre supprimé", null));
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

    private Long extractAssociationId(User user) {
        return user != null && user.getAssociation() != null ? user.getAssociation().getId() : null;
    }

}
