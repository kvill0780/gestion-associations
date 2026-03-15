package bf.kvill.associa.system.association.dto;

import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AssociationStatsDto {
    Long associationId;
    String associationName;
    Long totalMembers;
    Long activeMembers;
    Long inactiveMembers;
    Long totalPosts;
    Long activeMandates;
    Long totalRoles;
    AssociationStatus status;
    AssociationType type;
    LocalDateTime createdAt;
}