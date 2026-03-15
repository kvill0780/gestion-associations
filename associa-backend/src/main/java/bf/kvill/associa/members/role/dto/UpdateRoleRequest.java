package bf.kvill.associa.members.role.dto;

import bf.kvill.associa.shared.enums.RoleType;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class UpdateRoleRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private RoleType type;

    private Map<String, Boolean> permissions;

    private Integer displayOrder;

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
