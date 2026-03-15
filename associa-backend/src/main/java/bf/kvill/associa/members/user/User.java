package bf.kvill.associa.members.user;

import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.members.role.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entité représentant un utilisateur/membre d'une association
 *
 * Implémente UserDetails pour intégration Spring Security
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_association_id", columnList = "association_id"),
        @Index(name = "idx_users_membership_status", columnList = "membership_status"),
        @Index(name = "idx_users_is_super_admin", columnList = "is_super_admin")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    // ==================== Authentification ====================

    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String email;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(nullable = false)
    private String password;

    @Column(name = "remember_token", length = 100)
    private String rememberToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ==================== Profil ====================

    @Column(name = "first_name", nullable = false, length = 100)
    @ToString.Include
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @ToString.Include
    private String lastName;

    @Column(length = 20)
    private String whatsapp;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    // ==================== Adhésion ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status", nullable = false, length = 50)
    private MembershipStatus membershipStatus;

    @Column(name = "membership_date")
    private LocalDate membershipDate;

    // ==================== Association ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    @ToString.Exclude
    private Association association;

    // ==================== Super Admin ====================

    @Column(name = "is_super_admin", nullable = false)
    private Boolean isSuperAdmin;

    // ==================== Timestamps ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== Relations ====================

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Set<UserRole> userRoles;

    // ==================== Lifecycle Callbacks ====================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (membershipStatus == null) {
            membershipStatus = MembershipStatus.PENDING;
        }
        if (isSuperAdmin == null) {
            isSuperAdmin = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== UserDetails Implementation ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isSuperAdmin != null && isSuperAdmin) {
            return Set.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        }

        if (userRoles == null || userRoles.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_MEMBER"));
        }

        return userRoles.stream()
                .filter(UserRole::isCurrentlyValid)
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getSlug().toUpperCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return membershipStatus != MembershipStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return membershipStatus == MembershipStatus.ACTIVE;
    }

    // ==================== Business Methods ====================

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isSuperAdmin() {
        return Boolean.TRUE.equals(isSuperAdmin);
    }

    public boolean isActiveMember() {
        return MembershipStatus.ACTIVE.equals(membershipStatus);
    }

    public boolean isSuspended() {
        return membershipStatus == MembershipStatus.SUSPENDED;
    }

    public boolean isEmailVerified() {
        return emailVerifiedAt != null;
    }

    public void verifyEmail() {
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void activateMembership() {
        this.membershipStatus = MembershipStatus.ACTIVE;
        if (this.membershipDate == null) {
            this.membershipDate = LocalDate.now();
        }
    }

    public void suspend() {
        this.membershipStatus = MembershipStatus.SUSPENDED;
    }
}
