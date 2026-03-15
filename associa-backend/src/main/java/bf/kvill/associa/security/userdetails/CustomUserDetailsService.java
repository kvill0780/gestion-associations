package bf.kvill.associa.security.userdetails;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour charger les utilisateurs pour Spring Security
 * 
 * Implémente UserDetailsService de Spring Security
 * Utilisé par JwtAuthenticationFilter pour charger l'utilisateur
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son email (username)
     * 
     * Appelé automatiquement par Spring Security lors de :
     * - Validation JWT (JwtAuthenticationFilter)
     * - Login (AuthenticationManager)
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("🔍 Loading user by email: {}", email);

        // Chercher l'utilisateur avec ses rôles
        User user = userRepository.findByEmailWithUserRoles(email)
                .orElseThrow(() -> {
                    log.error("❌ User not found: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Vérifier si l'utilisateur est actif
        if (!user.isActiveMember()) {
            log.warn("User is not active: {}", email);
            throw new UsernameNotFoundException("User account is not active");
        }

        // Vérifier si l'association est active
        if (user.getAssociation() != null && !user.getAssociation().isActive()) {
            log.warn("User's association is not active: {}", email);
            throw new UsernameNotFoundException("Association is not active");
        }

        log.debug("User loaded successfully: {}", email);

        // Convertir en UserDetails Spring Security
        return buildUserDetails(user);
    }

    /**
     * Construit un objet UserDetails à partir d'un User
     */
    private UserDetails buildUserDetails(User user) {
        return CustomUserPrincipal.create(user, getAuthorities(user));
    }

    /**
     * Récupère les autorités (roles) d'un utilisateur
     * 
     * Format Spring Security : "ROLE_XXX"
     * Exemples : ROLE_SUPER_ADMIN, ROLE_PRESIDENT, ROLE_MEMBER
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Ajouter le rôle super admin si applicable
        if (user.isSuperAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            log.debug("Added ROLE_SUPER_ADMIN for user: {}", user.getEmail());
        }

        // Ajouter les rôles de l'utilisateur
        if (user.getUserRoles() != null) {
            user.getUserRoles().stream()
                    .filter(UserRole::isCurrentlyValid)
                    .filter(userRole -> userRole.getRole() != null)
                    .forEach(userRole -> {
                        String roleName = "ROLE_" + userRole.getRole().getSlug().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(roleName));
                        log.debug("Added {} for user: {}", roleName, user.getEmail());
                    });
        }

        // Si aucun rôle, ajouter ROLE_MEMBER par défaut
        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MEMBER"));
            log.debug("Added default ROLE_MEMBER for user: {}", user.getEmail());
        }

        return authorities;
    }

    /**
     * Charge un utilisateur par son ID
     * Utile pour les refresh tokens
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        log.debug("Loading user by ID: {}", userId);

        User user = userRepository.findByIdWithUserRoles(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        log.debug("User loaded successfully: {}", userId);
        return buildUserDetails(user);
    }
}
