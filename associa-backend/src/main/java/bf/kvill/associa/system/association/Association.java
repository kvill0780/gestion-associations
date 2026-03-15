package bf.kvill.associa.system.association;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.post.Post;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.mandate.Mandate;
import bf.kvill.associa.shared.enums.AssociationType;
import bf.kvill.associa.shared.enums.AssociationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Représente une association
 *
 * Une association est l'entité racine du système.
 * Elle contient :
 * - Des membres (Users)
 * - Des postes (Posts)
 * - Des rôles (Roles)
 * - Des mandats (Mandates)
 * - Et tous les autres modules (finances, événements, etc.)
 */
@Entity
@Table(name = "associations", indexes = {
        @Index(name = "idx_slug", columnList = "slug"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_by", columnList = "created_by_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SQLDelete(sql = "UPDATE associations SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Association {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    /**
     * Slug unique pour les URLs
     * Exemple : "miage-ouagadougou"
     */
    @Column(nullable = false, unique = true, length = 100)
    @ToString.Include
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Chemin du logo de l'association
     * Stocké dans le système de fichiers ou cloud storage
     */
    @Column(name = "logo_path")
    private String logoPath;

    /**
     * Email de contact officiel de l'association
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * Téléphone de contact
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * Adresse physique de l'association
     */
    @Column(length = 255)
    private String address;

    /**
     * Type d'association
     * STUDENT, PROFESSIONAL, CULTURAL, SPORTS, CHARITY, OTHER
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssociationType type;

    /**
     * Statut de l'association
     * ACTIVE, INACTIVE, SUSPENDED, ARCHIVED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssociationStatus status;

    /**
     * Cotisation par défaut (en FCFA ou autre devise)
     */
    @Column(name = "default_membership_fee", precision = 10, scale = 2)
    private BigDecimal defaultMembershipFee;

    /**
     * Durée de validité de l'adhésion en mois
     * Exemple : 12 mois (1 an)
     */
    @Column(name = "membership_validity_months")
    private Integer membershipValidityMonths;

    /**
     * Active ou non le workflow de double validation pour les finances
     * Si true : une transaction nécessite 2 approbations
     * Si false : une seule approbation suffit
     */
    @Column(name = "finance_approval_workflow")
    private Boolean financeApprovalWorkflow = false;

    /**
     * Active ou non l'approbation manuelle des nouveaux membres
     * Si true : les nouveaux membres restent en PENDING jusqu'à approbation
     * Si false : ils deviennent ACTIVE automatiquement
     */
    @Column(name = "auto_approve_members")
    @Builder.Default
    private Boolean autoApproveMembers = false;

    /**
     * Année de fondation
     */
    @Column(name = "founded_year")
    private Integer foundedYear;

    /**
     * Site web officiel
     */
    @Column(length = 255)
    private String website;

    /**
     * Réseaux sociaux (JSONB)
     * Exemple : {"facebook": "...", "twitter": "...", "instagram": "..."}
     */
    @Column(name = "social_links", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> socialLinks;

    /**
     * Métadonnées personnalisées (JSONB)
     * Permet d'ajouter des champs custom sans modifier le schéma
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    /**
     * Utilisateur qui a créé l'association
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== RELATIONS ====================

    /**
     * Membres de l'association
     */
    @OneToMany(mappedBy = "association", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> members = new HashSet<>();

    /**
     * Postes de l'association
     */
    @OneToMany(mappedBy = "association", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();

    /**
     * Rôles de l'association
     */
    @OneToMany(mappedBy = "association", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Mandats de l'association
     */
    @OneToMany(mappedBy = "association", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Mandate> mandates = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = AssociationStatus.ACTIVE;
        }
        if (financeApprovalWorkflow == null) {
            financeApprovalWorkflow = false;
        }
        if (autoApproveMembers == null) {
            autoApproveMembers = false;
        }
        if (defaultMembershipFee == null) {
            defaultMembershipFee = BigDecimal.valueOf(10000.00);
        }
        if (membershipValidityMonths == null) {
            membershipValidityMonths = 12;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Business Methods ====================

    /**
     * Vérifie si l'association est active
     */
    public boolean isActive() {
        return AssociationStatus.ACTIVE.equals(status);
    }

    /**
     * Vérifie si l'association est suspendue
     */
    public boolean isSuspended() {
        return AssociationStatus.SUSPENDED.equals(status);
    }

    /**
     * Vérifie si l'association est archivée
     */
    public boolean isArchived() {
        return AssociationStatus.ARCHIVED.equals(status);
    }

    /**
     * Vérifie si l'association utilise le workflow de double validation financière
     */
    public boolean requiresDoubleApproval() {
        return Boolean.TRUE.equals(financeApprovalWorkflow);
    }

    /**
     * Vérifie si les nouveaux membres sont approuvés automatiquement
     */
    public boolean autoApprovesMembers() {
        return Boolean.TRUE.equals(autoApproveMembers);
    }

    /**
     * Active l'association
     */
    public void activate() {
        this.status = AssociationStatus.ACTIVE;
    }

    /**
     * Suspend l'association
     */
    public void suspend() {
        this.status = AssociationStatus.SUSPENDED;
    }

    /**
     * Archive l'association
     */
    public void archive() {
        this.status = AssociationStatus.ARCHIVED;
    }

    /**
     * Compte le nombre de membres actifs
     */
    public long countActiveMembers() {
        if (members == null)
            return 0;
        return members.stream()
                .filter(User::isActiveMember)
                .count();
    }

    /**
     * Compte le nombre total de membres (actifs + inactifs)
     */
    public long countTotalMembers() {
        if (members == null)
            return 0;
        return members.size();
    }

    /**
     * Compte le nombre de postes
     */
    public long countPosts() {
        if (posts == null)
            return 0;
        return posts.size();
    }

    /**
     * Compte le nombre de rôles
     */
    public long countRoles() {
        if (roles == null)
            return 0;
        return roles.size();
    }

    /**
     * Compte le nombre de mandats actifs
     */
    public long countActiveMandates() {
        if (mandates == null)
            return 0;
        return mandates.stream()
                .filter(Mandate::isActive)
                .count();
    }

    /**
     * Vérifie si l'association a un logo
     */
    public boolean hasLogo() {
        return logoPath != null && !logoPath.trim().isEmpty();
    }

    /**
     * Vérifie si l'association a un site web
     */
    public boolean hasWebsite() {
        return website != null && !website.trim().isEmpty();
    }

    /**
     * Récupère le montant de la cotisation par défaut
     */
    public BigDecimal getMembershipFee() {
        return defaultMembershipFee != null
                ? defaultMembershipFee
                : BigDecimal.valueOf(10000.00);
    }

    /**
     * Récupère la durée de validité de l'adhésion
     */
    public Integer getMembershipValidity() {
        return membershipValidityMonths != null
                ? membershipValidityMonths
                : 12;
    }
}