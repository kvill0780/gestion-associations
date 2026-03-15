package bf.kvill.associa.security.auth;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.security.auth.dto.*;
import bf.kvill.associa.security.jwt.JwtService;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.core.security.permission.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import bf.kvill.associa.security.auth.RefreshTokenRepository;
import bf.kvill.associa.security.auth.PasswordResetTokenRepository;
import bf.kvill.associa.shared.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service d'authentification
 * 
 * Gère :
 * - Login (génération access + refresh tokens)
 * - Register (inscription nouveau utilisateur)
 * - Refresh token (renouvellement access token)
 * - Logout (révocation tokens)
 * - Forgot password (demande reset)
 * - Reset password (changement mot de passe)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

        private static final Logger log = LoggerFactory.getLogger(AuthService.class);

        private final UserRepository userRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final JwtService jwtService;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;
        private final AuditService auditService;
        private final PermissionService permissionService;
        private final EmailService emailService;
        @Value("${app.frontend.reset-password-url:http://localhost:5174/reset-password}")
        private String resetPasswordBaseUrl;

        // ==================== LOGIN ====================

        /**
         * Authentifie un utilisateur et retourne les tokens
         */
        @Transactional
        public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
                log.info("Login attempt for: {}", request.getEmail());

                try {
                        // Authentifier avec Spring Security
                        Authentication authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getEmail(),
                                                        request.getPassword()));

                        // Charger l'utilisateur
                        User user = userRepository.findByEmailWithUserRoles(request.getEmail())
                                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                        // Vérifier statut
                        if (!user.isActiveMember()) {
                                log.warn("Login failed: User not active - {}", request.getEmail());
                                throw new BadCredentialsException("Account is not active");
                        }

                        if (user.getAssociation() != null && !user.getAssociation().isActive()) {
                                log.warn("Login failed: Association not active - {}", request.getEmail());
                                throw new BadCredentialsException("Association is not active");
                        }

                        // Générer tokens
                        String accessToken = generateAccessToken(user);
                        String refreshToken = generateRefreshToken(user, httpRequest);

                        // Mettre à jour dernière connexion
                        user.setLastLoginAt(LocalDateTime.now());
                        userRepository.save(user);

                        // Audit
                        auditService.log(
                                        "LOGIN_SUCCESS",
                                        "User",
                                        user.getId(),
                                        user,
                                        Map.of("email", user.getEmail()));

                        log.info("Login successful for: {}", request.getEmail());

                        return LoginResponse.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(refreshToken)
                                        .tokenType("Bearer")
                                        .expiresIn(86400L) // 24h en secondes
                                        .user(buildUserInfo(user))
                                        .build();

                } catch (BadCredentialsException e) {
                        log.error("❌ Login failed: Invalid credentials - {}", request.getEmail());

                        // Audit échec
                        auditService.logWarning(
                                        "LOGIN_FAILED",
                                        null,
                                        null,
                                        (Long) null,
                                        Map.of("email", request.getEmail(), "reason", "Invalid credentials"));

                        throw new BadCredentialsException("Invalid email or password");
                }
        }

        // ==================== REGISTER ====================

        /**
         * Inscrit un nouvel utilisateur
         */
        @Transactional
        public RegisterResponse register(RegisterRequest request) {
                log.info("Registration attempt for: {}", request.getEmail());

                // Vérifier email unique
                if (userRepository.existsByEmail(request.getEmail())) {
                        log.warn("Registration failed: Email already exists - {}", request.getEmail());
                        throw new IllegalArgumentException("Email already registered");
                }

                // Créer utilisateur
                User user = User.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .whatsapp(request.getPhone())
                                .membershipStatus(MembershipStatus.PENDING) // En attente d'approbation
                                .isSuperAdmin(false)
                                .build();

                // TODO: Associer à une association si fournie
                // if (request.getAssociationId() != null) { ... }

                User savedUser = userRepository.save(user);

                // Audit
                auditService.log(
                                "USER_REGISTERED",
                                "User",
                                savedUser.getId(),
                                (Long) null,
                                Map.of(
                                                "email", savedUser.getEmail(),
                                                "name", savedUser.getFullName()));

                log.info("Registration successful for: {}", request.getEmail());

                return RegisterResponse.builder()
                                .message("Registration successful. Please wait for admin approval.")
                                .userId(savedUser.getId())
                                .email(savedUser.getEmail())
                                .membershipStatus(savedUser.getMembershipStatus())
                                .build();
        }

        // ==================== REFRESH TOKEN ====================

        /**
         * Génère un nouveau access token à partir d'un refresh token
         */
        @Transactional
        public RefreshTokenResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
                log.info("Refresh token attempt");

                // Valider refresh token
                RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

                if (!refreshToken.isValid()) {
                        log.warn("Refresh token invalid or expired");
                        throw new IllegalArgumentException("Refresh token expired or revoked");
                }

                // Charger l'utilisateur
                User user = refreshToken.getUser();

                // Générer nouveau access token
                String newAccessToken = generateAccessToken(user);

                // Optionnel : Générer nouveau refresh token (rotation)
                String newRefreshToken = null;
                if (request.getRotateRefreshToken() != null && request.getRotateRefreshToken()) {
                        refreshToken.revoke();
                        refreshTokenRepository.save(refreshToken);
                        newRefreshToken = generateRefreshToken(user, httpRequest);
                } else {
                        // Mettre à jour dernière utilisation
                        refreshToken.updateLastUsed();
                        refreshTokenRepository.save(refreshToken);
                }

                log.info("Token refreshed for user: {}", user.getEmail());

                return RefreshTokenResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken != null ? newRefreshToken : request.getRefreshToken())
                                .tokenType("Bearer")
                                .expiresIn(86400L)
                                .build();
        }

        // ==================== LOGOUT ====================

        /**
         * Déconnecte un utilisateur (révoque son refresh token)
         */
        @Transactional
        public void logout(LogoutRequest request, Long userId) {
                log.info("Logout for user: {}", userId);

                // Révoquer le refresh token
                if (request.getRefreshToken() != null) {
                        refreshTokenRepository.findByToken(request.getRefreshToken())
                                        .ifPresent(token -> {
                                                token.revoke();
                                                refreshTokenRepository.save(token);
                                        });
                }

                // Optionnel : Révoquer tous les tokens de l'utilisateur
                if (request.getRevokeAllTokens() != null && request.getRevokeAllTokens()) {
                        refreshTokenRepository.revokeAllByUserId(userId);
                }

                // Audit
                auditService.log(
                                "LOGOUT",
                                "User",
                                userId,
                                userId,
                                Map.of("revokeAll",
                                                request.getRevokeAllTokens() != null && request.getRevokeAllTokens()));

                log.info("Logout successful for user: {}", userId);
        }

        // ==================== FORGOT PASSWORD ====================

        /**
         * Génère un token de reset et envoie l'email
         */
        @Transactional
        public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request, HttpServletRequest httpRequest) {
                log.info("Forgot password request for: {}", request.getEmail());

                // Recherche optionnelle pour éviter l'énumération d'emails
                var userOpt = userRepository.findByEmail(request.getEmail());

                if (userOpt.isEmpty()) {
                        // Email inconnu : réponse neutre (anti-énumération)
                        log.info("Forgot password requested for non-existing email: {}", request.getEmail());

                        // Audit optionnel (sans userId)
                        auditService.logWarning(
                                        "PASSWORD_RESET_REQUESTED_UNKNOWN_EMAIL",
                                        null,
                                        null,
                                        (Long) null,
                                        Map.of("email", request.getEmail()));

                        return ForgotPasswordResponse.builder()
                                        .message("Password reset email sent")
                                        .email(request.getEmail())
                                        .build();
                }

                User user = userOpt.get();

                // Invalider anciens tokens
                passwordResetTokenRepository.invalidateAllByUserId(user.getId());

                // Créer nouveau token
                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .token(UUID.randomUUID().toString())
                                .user(user)
                                .expiresAt(LocalDateTime.now().plusHours(1))
                                .ipAddress(getClientIp(httpRequest))
                                .build();

                passwordResetTokenRepository.save(resetToken);

                // Génération du lien de reset (pointe vers le frontend React)
                String resetLink = buildResetPasswordLink(resetToken.getToken());

                // Appel au service d'email. La méthode étant @Async, elle s'exécute dans un
                // autre thread
                // et ne bloque pas la réponse HTTP.
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

                // Audit
                auditService.log(
                                "PASSWORD_RESET_REQUESTED",
                                "User",
                                user.getId(),
                                user,
                                Map.of("email", user.getEmail()));

                log.info("Password reset email sent to: {}", request.getEmail());

                return ForgotPasswordResponse.builder()
                                .message("Password reset email sent")
                                .email(request.getEmail())
                                .build();
        }

        // ==================== RESET PASSWORD ====================

        /**
         * Change le mot de passe via token
         */
        @Transactional
        public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
                log.info("Password reset attempt with token");

                // Valider token
                PasswordResetToken resetToken = passwordResetTokenRepository
                                .findValidToken(request.getToken(), LocalDateTime.now())
                                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

                User user = resetToken.getUser();

                // Changer mot de passe
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                // Marquer token comme utilisé
                resetToken.markAsUsed();
                passwordResetTokenRepository.save(resetToken);

                // Révoquer tous les refresh tokens (forcer reconnexion)
                refreshTokenRepository.revokeAllByUserId(user.getId());

                // Audit
                auditService.log(
                                "PASSWORD_RESET_SUCCESS",
                                "User",
                                user.getId(),
                                user,
                                Map.of("email", user.getEmail()));

                log.info("✅ Password reset successful for: {}", user.getEmail());

                return ResetPasswordResponse.builder()
                                .message("Password changed successfully")
                                .email(user.getEmail())
                                .build();
        }

        // ==================== HELPERS ====================

        private String generateAccessToken(User user) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

                // Récupérer les rôles
                String roles = String.join(",",
                                userDetails.getAuthorities().stream()
                                                .map(auth -> auth.getAuthority())
                                                .toList());

                return jwtService.generateAccessToken(
                                userDetails,
                                user.getId(),
                                user.getAssociation() != null ? user.getAssociation().getId() : null,
                                roles);
        }

        private String generateRefreshToken(User user, HttpServletRequest httpRequest) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

                String tokenString = jwtService.generateRefreshToken(userDetails, user.getId());

                // Sauvegarder en base
                RefreshToken refreshToken = RefreshToken.builder()
                                .token(tokenString)
                                .user(user)
                                .expiresAt(LocalDateTime.now().plusDays(30))
                                .ipAddress(getClientIp(httpRequest))
                                .userAgent(httpRequest.getHeader("User-Agent"))
                                .build();

                refreshTokenRepository.save(refreshToken);

                return tokenString;
        }

        private UserInfoDto buildUserInfo(User user) {
                List<String> roles = user.getUserRoles() == null
                        ? List.of()
                        : user.getUserRoles().stream()
                                .filter(userRole -> userRole.isCurrentlyValid())
                                .map(userRole -> userRole.getRole())
                                .filter(role -> role != null)
                                .map(role -> role.getName())
                                .distinct()
                                .toList();
                List<String> permissions = permissionService.getUserPermissions(user.getId()).stream().toList();
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
                                .permissions(permissions)
                                .associationId(user.getAssociation() != null ? user.getAssociation().getId() : null)
                                .associationName(user.getAssociation() != null ? user.getAssociation().getName() : null)
                                .build();
        }

        private String getClientIp(HttpServletRequest request) {
                String[] headers = {
                                "X-Forwarded-For",
                                "Proxy-Client-IP",
                                "WL-Proxy-Client-IP",
                                "HTTP_X_FORWARDED_FOR",
                                "HTTP_X_FORWARDED",
                                "HTTP_FORWARDED_FOR",
                                "HTTP_FORWARDED",
                                "REMOTE_ADDR"
                };

                for (String header : headers) {
                        String ip = request.getHeader(header);
                        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                                return ip.split(",")[0].trim();
                        }
                }

                return request.getRemoteAddr();
        }

        private String buildResetPasswordLink(String token) {
                String separator = resetPasswordBaseUrl.contains("?") ? "&" : "?";
                String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
                return resetPasswordBaseUrl + separator + "token=" + encodedToken;
        }
}
