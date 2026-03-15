
package bf.kvill.associa.members.role.dto;

import bf.kvill.associa.shared.enums.RoleType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class RoleResponseDto {
    Long id;
    String name;
    String slug;
    String description;
    RoleType type;
    Map<String, Boolean> permissions;
    Boolean isTemplate;
    Integer displayOrder;
    Long associationId;
    LocalDateTime createdAt;
}