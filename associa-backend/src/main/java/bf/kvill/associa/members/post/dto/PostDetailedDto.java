package bf.kvill.associa.members.post.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO détaillé d'un poste (avec rôles suggérés)
 */
@Value
@Builder
public class PostDetailedDto {
    Long id;
    String name;
    String description;
    Boolean isExecutive;
    Integer maxOccupants;
    Integer displayOrder;
    Integer mandateDurationMonths;
    Boolean requiresElection;
    Boolean isActive;
    Long associationId;
    String associationName;
    RoleInfo defaultRole;
    List<RoleInfo> suggestedRoles;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Value
    public static class RoleInfo {
        Long roleId;
        String roleName;
        String roleSlug;
    }
}
