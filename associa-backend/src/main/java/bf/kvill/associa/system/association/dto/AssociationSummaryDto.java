package bf.kvill.associa.system.association.dto;

import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AssociationSummaryDto {
    Long id;
    String name;
    String slug;
    String logoPath;
    AssociationType type;
    AssociationStatus status;
    Long activeMembersCount;
    Long totalMembersCount;
    String contactEmail;
}