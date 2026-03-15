package bf.kvill.associa.members.post.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un poste (standard)
 */
@Value
@Builder
public class PostResponseDto {
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
    Integer suggestedRolesCount;
    Long defaultRoleId;
    String defaultRoleName;
    String defaultRoleSlug;
    Boolean canAcceptNewMandate;
    String fullDescription;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
