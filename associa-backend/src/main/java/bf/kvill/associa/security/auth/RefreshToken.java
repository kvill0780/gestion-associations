package bf.kvill.associa.security.auth;

import bf.kvill.associa.members.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité pour stocker les refresh tokens
 * 
 * Un refresh token permet d'obtenir un nouveau access token
 * sans redemander les credentials (email/password)
 * 
 * Durée de vie : 30 jours (vs 24h pour access token)
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token"),
        @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    /**
     * Token JWT (hash unique)
     */
    @Column(nullable = false, unique = true, length = 512)
    @ToString.Include
    private String token;

    /**
     * Utilisateur propriétaire du token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Date d'expiration
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Date de création
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Adresse IP du client
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User Agent (navigateur/app)
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Token révoqué manuellement ?
     */
    @Column(nullable = false)
    private Boolean revoked = false;

    /**
     * Date de dernière utilisation
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (revoked == null) {
            revoked = false;
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
     * (non expiré et non révoqué)
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Révoque le token
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Met à jour la date de dernière utilisation
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
