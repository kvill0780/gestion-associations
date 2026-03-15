package bf.kvill.associa.security.auth;

import bf.kvill.associa.members.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité pour gérer la récupération de mot de passe
 * 
 * Workflow :
 * 1. User demande reset via email
 * 2. Système génère token unique + email
 * 3. User clique sur lien dans email
 * 4. User saisit nouveau mot de passe
 * 5. Token est consommé (used = true)
 * 
 * Durée de vie : 1 heure
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_tokens_token", columnList = "token"),
        @Index(name = "idx_password_reset_tokens_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    /**
     * Token unique (UUID)
     */
    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String token;

    /**
     * Utilisateur qui a demandé le reset
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * Date d'expiration (1 heure par défaut)
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Token déjà utilisé ?
     */
    @Column(nullable = false)
    private Boolean used = false;

    /**
     * Date d'utilisation
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Adresse IP de la demande
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        if (expiresAt == null) {
            // Expire dans 1 heure
            expiresAt = LocalDateTime.now().plusHours(1);
        }
        if (used == null) {
            used = false;
        }
    }

    // ==================== Business Methods ====================

    /**
     * Vérifie si le token est expiré
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Vérifie si le token est valide
     * (non expiré et non utilisé)
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }

    /**
     * Marque le token comme utilisé
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
