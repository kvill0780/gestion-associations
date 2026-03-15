package bf.kvill.associa.members.role;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles", indexes = {
        @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
        @Index(name = "idx_user_roles_role_id", columnList = "role_id"),
        @Index(name = "idx_user_roles_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "assigned_by_id")
    private Long assignedById;

    @Column(name = "term_start")
    private LocalDate termStart;

    @Column(name = "term_end")
    private LocalDate termEnd;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==================== Business Methods ====================

    public boolean isActiveFlag() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isCurrentlyValid() {
        if (!isActiveFlag()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (termStart != null && termStart.isAfter(today)) {
            return false;
        }
        if (termEnd != null && termEnd.isBefore(today)) {
            return false;
        }
        return true;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Long getAssignedById() {
        return assignedById;
    }

    public void setAssignedById(Long assignedById) {
        this.assignedById = assignedById;
    }

    public LocalDate getTermStart() {
        return termStart;
    }

    public void setTermStart(LocalDate termStart) {
        this.termStart = termStart;
    }

    public LocalDate getTermEnd() {
        return termEnd;
    }

    public void setTermEnd(LocalDate termEnd) {
        this.termEnd = termEnd;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    // ==================== Manual Builder (Lombok fallback) ====================

    public static UserRoleBuilder builder() {
        return new UserRoleBuilder();
    }

    public static class UserRoleBuilder {
        private Long id;
        private Long userId;
        private Long roleId;
        private Role role;
        private LocalDateTime assignedAt;
        private Long assignedById;
        private LocalDate termStart;
        private LocalDate termEnd;
        private Boolean isActive;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        UserRoleBuilder() {
        }

        public UserRoleBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserRoleBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserRoleBuilder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserRoleBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserRoleBuilder assignedAt(LocalDateTime assignedAt) {
            this.assignedAt = assignedAt;
            return this;
        }

        public UserRoleBuilder assignedById(Long assignedById) {
            this.assignedById = assignedById;
            return this;
        }

        public UserRoleBuilder termStart(LocalDate termStart) {
            this.termStart = termStart;
            return this;
        }

        public UserRoleBuilder termEnd(LocalDate termEnd) {
            this.termEnd = termEnd;
            return this;
        }

        public UserRoleBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public UserRoleBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public UserRoleBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserRoleBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserRole build() {
            UserRole userRole = new UserRole();
            userRole.setId(id);
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setRole(role);
            userRole.setAssignedAt(assignedAt != null ? assignedAt : LocalDateTime.now());
            userRole.setAssignedById(assignedById);
            userRole.setTermStart(termStart);
            userRole.setTermEnd(termEnd);
            userRole.setIsActive(isActive != null ? isActive : true);
            userRole.setNotes(notes);
            userRole.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
            userRole.setUpdatedAt(updatedAt != null ? updatedAt : LocalDateTime.now());
            return userRole;
        }
    }
}
