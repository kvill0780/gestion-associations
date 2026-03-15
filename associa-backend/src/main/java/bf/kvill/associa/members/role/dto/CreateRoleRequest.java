package bf.kvill.associa.members.role.dto;

import bf.kvill.associa.shared.enums.RoleType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Le slug est obligatoire")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug invalide")
    @Size(max = 100)
    private String slug;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Le type est obligatoire")
    private RoleType type;

    @NotNull(message = "Les permissions sont obligatoires")
    private Map<String, Boolean> permissions;

    private Boolean isTemplate;

    private Integer displayOrder;

    @NotNull(message = "L'association est obligatoire")
    private Long associationId;

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

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

    public Long getAssociationId() {
        return associationId;
    }

    public void setAssociationId(Long associationId) {
        this.associationId = associationId;
    }
}