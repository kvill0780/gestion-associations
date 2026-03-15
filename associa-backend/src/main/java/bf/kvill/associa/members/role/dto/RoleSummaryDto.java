package bf.kvill.associa.members.role.dto;

import bf.kvill.associa.shared.enums.RoleType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RoleSummaryDto {
    Long id;
    String name;
    String slug;
    RoleType type;
}