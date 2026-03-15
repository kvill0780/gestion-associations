package bf.kvill.associa.security.auth;

import bf.kvill.associa.security.auth.dto.*;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserService;
import bf.kvill.associa.members.user.dto.UpdateMemberRequest;
import bf.kvill.associa.core.security.permission.PermissionService;
import bf.kvill.associa.members.mandate.MandateService;
import bf.kvill.associa.members.mandate.mapper.MandateMapper;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Contrôleur REST pour l'authentification
 * 
 * Endpoints :
 * - POST /api/auth/register - Inscription
 * - POST /api/auth/login - Connexion
 * - POST /api/auth/refresh - Rafraîchir token
 * - POST /api/auth/logout - Déconnexion
 * - POST /api/auth/forgot-password - Demander reset mot de passe
 * - POST /api/auth/reset-password - Changer mot de passe
 * - GET /api/auth/me - Infos utilisateur connecté
 * - PUT /api/auth/me/password - Changer le mot de passe
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion, gestion des tokens et mot de passe")
public class AuthController {

        private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

        private final AuthService authService;
        private final UserService userService;
        private final PermissionService permissionService;
        private final MandateService mandateService;
        private final MandateMapper mandateMapper;

        /**
         * Inscription d'un nouvel utilisateur
         * 
         * Status : PENDING (en attente d'approbation par admin)
         */
        @Operation(summary = "Inscription", description = "Crée un compte en statut PENDING. Nécessite approbation admin.")
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<RegisterResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                RegisterResponse response = authService.register(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Registration successful", response));
        }

        /**
         * Connexion utilisateur
         * 
         * Retourne :
         * - accessToken (24h)
         * - refreshToken (30 jours)
         * - Infos utilisateur
         */
        @Operation(summary = "Connexion", description = "Retourne accessToken (24h) + refreshToken (30j)")
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        HttpServletRequest httpRequest) {
                LoginResponse response = authService.login(request, httpRequest);

                return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        }

        /**
         * Rafraîchir l'access token
         * 
         * Utilise un refresh token pour obtenir un nouvel access token
         * sans redemander email/password
         */
        @Operation(summary = "Rafraîchir le token", description = "Échange un refresh token contre un nouvel access token")
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request,
                        HttpServletRequest httpRequest) {
                RefreshTokenResponse response = authService.refreshToken(request, httpRequest);

                return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
        }

        /**
         * Déconnexion
         * 
         * Révoque le refresh token
         * Optionnel : Révoquer tous les tokens (revokeAllTokens: true)
         */
        @Operation(summary = "Déconnexion", description = "Révoque le refresh token", security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestBody LogoutRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {
                // Extraire userId depuis email
                // TODO: Améliorer en stockant userId dans le principal
                Long userId = getUserIdFromPrincipal(userDetails);

                authService.logout(request, userId);

                return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
        }

        /**
         * Demander un reset de mot de passe
         * 
         * Envoie un email avec un lien de reset (valide 1h)
         */
        @Operation(summary = "Demander un reset de mot de passe", description = "Envoie un email avec lien valide 1h (anti-énumération : réponse identique si email inconnu)")
        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequest request,
                        HttpServletRequest httpRequest) {
                ForgotPasswordResponse response = authService.forgotPassword(request, httpRequest);

                return ResponseEntity.ok(ApiResponse.success(
                                "If this email exists, a reset link has been sent", response));
        }

        /**
         * Changer le mot de passe via token
         * 
         * Le token est fourni dans le lien de l'email
         */
        @Operation(summary = "Réinitialiser le mot de passe", description = "Change le mot de passe via le token reçu par email")
        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequest request) {
                ResetPasswordResponse response = authService.resetPassword(request);

                return ResponseEntity.ok(ApiResponse.success("Password changed successfully", response));
        }

        /**
         * Récupérer les informations de l'utilisateur connecté
         * 
         * Nécessite un token valide
         */
        @Operation(summary = "Profil utilisateur connecté", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<UserInfoDto>> getCurrentUser(
                        @AuthenticationPrincipal UserDetails userDetails) {
                Long userId = getUserIdFromPrincipal(userDetails);
                User user = userService.findByIdWithUserRoles(userId);

                return ResponseEntity.ok(ApiResponse.success("User info retrieved", buildUserInfo(user, userId)));
        }

        /**
         * Mettre à jour le profil utilisateur connecté
         */
        @Operation(summary = "Mettre à jour le profil", security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/me")
        public ResponseEntity<ApiResponse<UserInfoDto>> updateProfile(
                        @Valid @RequestBody UpdateMemberRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Long userId = getUserIdFromPrincipal(userDetails);
                userService.updateUser(userId, request);

                User updatedUser = userService.findByIdWithUserRoles(userId);
                return ResponseEntity.ok(
                                ApiResponse.success("Profile updated", buildUserInfo(updatedUser, userId)));
        }

        /**
         * Changer le mot de passe de l'utilisateur connecté
         */
        @Operation(summary = "Changer le mot de passe", security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/me/password")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @Valid @RequestBody ChangePasswordRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Long userId = getUserIdFromPrincipal(userDetails);
                userService.changePasswordWithCurrent(userId, request.getCurrentPassword(), request.getNewPassword());

                return ResponseEntity.ok(ApiResponse.success("Password updated", null));
        }

        /**
         * Vérifier si un token est valide
         * 
         * Endpoint de diagnostic
         */
        @Operation(summary = "Valider un token", description = "Retourne true si le token JWT est valide", security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/validate")
        public ResponseEntity<ApiResponse<Boolean>> validateToken(
                        @AuthenticationPrincipal UserDetails userDetails) {
                boolean isValid = userDetails != null;

                return ResponseEntity.ok(ApiResponse.success(
                                isValid ? "Token is valid" : "Token is invalid",
                                isValid));
        }

        // ==================== HELPERS ====================

        /**
         * Récupère l'userId depuis le UserDetails
         * 
         * TODO: Améliorer en créant un CustomUserPrincipal qui contient userId
         */
        private Long getUserIdFromPrincipal(UserDetails userDetails) {
                if (userDetails instanceof CustomUserPrincipal) {
                        return ((CustomUserPrincipal) userDetails).getId();
                }
                throw new IllegalStateException("Invalid UserDetails type");
        }

        private UserInfoDto buildUserInfo(User user, Long userId) {
                List<String> roles = List.of();
                if (user.getUserRoles() != null && PERSISTENCE_UTIL.isLoaded(user, "userRoles")) {
                        roles = user.getUserRoles().stream()
                                        .filter(userRole -> userRole.isCurrentlyValid())
                                        .map(userRole -> userRole.getRole())
                                        .filter(role -> role != null)
                                        .map(role -> role.getName())
                                        .distinct()
                                        .toList();
                }

                return UserInfoDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .fullName(user.getFullName())
                                .phone(user.getWhatsapp())
                                .membershipStatus(user.getMembershipStatus())
                                .isSuperAdmin(user.isSuperAdmin())
                                .roles(roles)
                                .permissions(permissionService.getUserPermissions(userId).stream().toList())
                                .currentMandates(mandateService.findActiveUserMandates(userId).stream()
                                                .map(mandateMapper::toResponseDto)
                                                .toList())
                                .associationId(user.getAssociation() != null ? user.getAssociation().getId() : null)
                                .associationName(user.getAssociation() != null ? user.getAssociation().getName() : null)
                                .build();
        }
}
