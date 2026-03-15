// ==================== modules/members/post/Post.java ====================

package bf.kvill.associa.members.post;

import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.members.role.Role;
import bf.kvill.associa.members.mandate.Mandate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Représente un poste statutaire dans une association
 *
 * Exemples : Président, Vice-Président, Trésorier, Secrétaire Général, etc.
 *
 * Un poste est différent d'un rôle :
 * - POST = Titre officiel dans l'organisation
 * - ROLE = Ensemble de permissions techniques
 *
 * Un poste peut suggérer un ou plusieurs rôles via POST_ROLE
 * et désigner un rôle par défaut explicite (default_role_id).
 */
@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_association_id", columnList = "association_id"),
        @Index(name = "idx_posts_name", columnList = "name"),
        @Index(name = "idx_posts_is_executive", columnList = "is_executive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @Column(nullable = false, length = 100)
    @ToString.Include
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Indique si ce poste fait partie du bureau exécutif
     */
    @Column(name = "is_executive", nullable = false)
    private Boolean isExecutive = false;

    /**
     * Nombre maximum de personnes pouvant occuper ce poste simultanément
     */
    @Column(name = "max_occupants")
    private Integer maxOccupants = 1;

    /**
     * Ordre d'affichage (pour lister les postes dans l'ordre hiérarchique)
     */
    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Durée standard du mandat en mois
     */
    @Column(name = "mandate_duration_months")
    private Integer mandateDurationMonths;

    /**
     * Le poste nécessite-t-il une élection formelle ?
     */
    @Column(name = "requires_election")
    @Builder.Default
    private Boolean requiresElection = false;

    /**
     * Le poste est-il actuellement actif ?
     * (permet de désactiver temporairement un poste sans le supprimer)
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Rôles suggérés pour ce poste (relation N-N)
     *
     * Exemple : Poste "Président" peut suggérer le rôle "Admin Complet"
     *
     * Lors de l'attribution du poste, le système peut automatiquement
     * attribuer un de ces rôles à la personne nommée
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_roles", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Rôle par défaut explicitement recommandé pour ce poste.
     * Ce rôle est prioritaire lors de l'assignation automatique via mandat.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_role_id")
    private Role defaultRole;

    /**
     * Historique des mandats pour ce poste
     */
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Mandate> mandates = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (isExecutive == null)
            isExecutive = false;
        if (maxOccupants == null)
            maxOccupants = 1;
        if (displayOrder == null)
            displayOrder = 0;
        if (requiresElection == null)
            requiresElection = false;
        if (isActive == null)
            isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Association getAssociation() {
        return association;
    }

    public void setAssociation(Association association) {
        this.association = association;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsExecutive() {
        return isExecutive;
    }

    public void setIsExecutive(Boolean isExecutive) {
        this.isExecutive = isExecutive;
    }

    public Integer getMaxOccupants() {
        return maxOccupants;
    }

    public void setMaxOccupants(Integer maxOccupants) {
        this.maxOccupants = maxOccupants;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getMandateDurationMonths() {
        return mandateDurationMonths;
    }

    public void setMandateDurationMonths(Integer mandateDurationMonths) {
        this.mandateDurationMonths = mandateDurationMonths;
    }

    public Boolean getRequiresElection() {
        return requiresElection;
    }

    public void setRequiresElection(Boolean requiresElection) {
        this.requiresElection = requiresElection;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Role getDefaultRole() {
        return defaultRole;
    }

    public void setDefaultRole(Role defaultRole) {
        this.defaultRole = defaultRole;
    }

    public Set<Mandate> getMandates() {
        return mandates;
    }

    public void setMandates(Set<Mandate> mandates) {
        this.mandates = mandates;
    }

    // ==================== Business Methods ====================

    /**
     * Vérifie si ce poste fait partie du bureau exécutif
     */
    public boolean isExecutive() {
        return Boolean.TRUE.equals(isExecutive);
    }

    /**
     * Vérifie si ce poste est actuellement actif
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Vérifie si ce poste nécessite une élection
     */
    public boolean requiresElection() {
        return Boolean.TRUE.equals(requiresElection);
    }

    /**
     * Vérifie si ce poste a une limite d'occupants
     */
    public boolean hasOccupantLimit() {
        return maxOccupants != null && maxOccupants > 0;
    }

    /**
     * Vérifie si ce poste peut accueillir plusieurs occupants simultanés
     */
    public boolean allowsMultipleOccupants() {
        return maxOccupants != null && maxOccupants > 1;
    }

    /**
     * Vérifie si le poste a des rôles suggérés
     */
    public boolean hasSuggestedRoles() {
        return roles != null && !roles.isEmpty();
    }

    /**
     * Récupère le rôle par défaut effectif (explicite, sinon fallback trié).
     */
    public Role getEffectiveDefaultRole() {
        if (defaultRole != null) {
            return defaultRole;
        }

        if (roles == null || roles.isEmpty()) {
            return null;
        }

        return roles.stream()
                .sorted((a, b) -> {
                    Integer aOrder = a.getDisplayOrder() != null ? a.getDisplayOrder() : Integer.MAX_VALUE;
                    Integer bOrder = b.getDisplayOrder() != null ? b.getDisplayOrder() : Integer.MAX_VALUE;
                    int cmp = Integer.compare(aOrder, bOrder);
                    if (cmp != 0) {
                        return cmp;
                    }
                    Long aId = a.getId() != null ? a.getId() : Long.MAX_VALUE;
                    Long bId = b.getId() != null ? b.getId() : Long.MAX_VALUE;
                    return Long.compare(aId, bId);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Ajoute un rôle suggéré à ce poste
     */
    public void addSuggestedRole(Role role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    /**
     * Retire un rôle suggéré de ce poste
     */
    public void removeSuggestedRole(Role role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    /**
     * Désactive ce poste
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Active ce poste
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Compte le nombre de mandats actifs pour ce poste
     */
    public long countActiveMandates() {
        if (mandates == null)
            return 0;
        return mandates.stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .count();
    }

    /**
     * Vérifie si le poste peut accepter un nouveau mandataire
     */
    public boolean canAcceptNewMandate() {
        if (!isActive())
            return false;
        if (!hasOccupantLimit())
            return true; // Pas de limite

        long activeCount = countActiveMandates();
        return activeCount < maxOccupants;
    }

    /**
     * Récupère la description complète du poste
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        if (isExecutive()) {
            sb.append(" (Bureau Exécutif)");
        }

        if (hasOccupantLimit()) {
            sb.append(" - Max ").append(maxOccupants).append(" occupant(s)");
        }

        if (mandateDurationMonths != null) {
            sb.append(" - Mandat de ").append(mandateDurationMonths).append(" mois");
        }

        return sb.toString();
    }
}
