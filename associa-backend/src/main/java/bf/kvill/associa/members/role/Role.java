package bf.kvill.associa.members.role;

import bf.kvill.associa.shared.enums.RoleType;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.shared.converter.PermissionsConverter;
import bf.kvill.associa.members.post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_roles_association_id", columnList = "association_id"),
        @Index(name = "idx_roles_slug", columnList = "slug"),
        @Index(name = "idx_roles_name", columnList = "name"),
        @Index(name = "idx_roles_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {

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

    @Column(nullable = false, length = 100)
    @ToString.Include
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleType type;

    /**
     * Permissions stockées en JSONB
     * Conversion automatique Map ↔ JSONB via PermissionsConverter
     */
    @Convert(converter = PermissionsConverter.class)
    @Column(columnDefinition = "jsonb", name = "permissions", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> permissions;

    @Column(name = "is_default")
    private Boolean isTemplate = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (isTemplate == null)
            isTemplate = false;
        if (displayOrder == null)
            displayOrder = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    // ==================== Manual Builder (Lombok fallback) ====================

    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private Long id;
        private Association association;
        private String name;
        private String slug;
        private String description;
        private RoleType type;
        private Map<String, Boolean> permissions;
        private Boolean isTemplate;
        private Integer displayOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Set<Post> posts;

        RoleBuilder() {
        }

        public RoleBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RoleBuilder association(Association association) {
            this.association = association;
            return this;
        }

        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoleBuilder type(RoleType type) {
            this.type = type;
            return this;
        }

        public RoleBuilder permissions(Map<String, Boolean> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RoleBuilder isTemplate(Boolean isTemplate) {
            this.isTemplate = isTemplate;
            return this;
        }

        public RoleBuilder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public RoleBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RoleBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public RoleBuilder posts(Set<Post> posts) {
            this.posts = posts;
            return this;
        }

        public Role build() {
            Role role = new Role();
            role.setId(id);
            role.setAssociation(association);
            role.setName(name);
            role.setSlug(slug);
            role.setDescription(description);
            role.setType(type);
            role.setPermissions(permissions);
            role.setIsTemplate(isTemplate);
            role.setDisplayOrder(displayOrder);
            role.setCreatedAt(createdAt);
            role.setUpdatedAt(updatedAt);
            role.setPosts(posts != null ? posts : new HashSet<>());
            return role;
        }
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoleType getType() {
        return type;
    }

    public void setType(RoleType type) {
        this.type = type;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
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

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
    }
}
