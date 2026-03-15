package bf.kvill.associa.members.mandate;

import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.post.Post;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité Mandat - Représente l'attribution d'un poste à un membre
 * Un mandat lie un utilisateur à un poste pour une période donnée
 * 
 * ⚠️ Contrainte unique : Un utilisateur ne peut avoir qu'un seul mandat actif
 * par poste
 * Implémentée via index SQL partiel : UNIQUE (user_id, post_id) WHERE active =
 * true
 */
@Entity
@Table(name = "mandates", indexes = {
        @Index(name = "idx_mandates_association_id", columnList = "association_id"),
        @Index(name = "idx_mandates_user_id", columnList = "user_id"),
        @Index(name = "idx_mandates_post_id", columnList = "post_id"),
        @Index(name = "idx_mandates_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Mandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "association_id", nullable = false)
    private Association association;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @ToString.Include
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "assigned_by_id")
    private Long assignedById;

    /**
     * Utilisateur qui a attribué ce mandat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", insertable = false, updatable = false)
    private User assignedBy;

    /**
     * Indique si un rôle a été automatiquement attribué lors de ce mandat
     * Utilisé pour la révocation automatique du rôle à la fin du mandat
     */
    @Column(name = "assign_role")
    private Boolean assignRole = false;

    /**
     * ID du rôle attribué lors de ce mandat
     * Permet de savoir exactement quel rôle révoquer (peut différer du rôle suggéré
     * si override)
     */
    @Column(name = "assigned_role_id")
    private Long assignedRoleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }

    public Boolean getAssignRole() {
        return assignRole;
    }

    public void setAssignRole(Boolean assignRole) {
        this.assignRole = assignRole;
    }

    public Long getAssignedRoleId() {
        return assignedRoleId;
    }

    public void setAssignedRoleId(Long assignedRoleId) {
        this.assignedRoleId = assignedRoleId;
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

    // ==================== Business Methods ====================

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean isExpired() {
        if (endDate == null)
            return false;
        return endDate.isBefore(LocalDate.now());
    }

    public void deactivate() {
        this.active = false;
        if (this.endDate == null) {
            this.endDate = LocalDate.now();
        }
    }
}
