package bf.kvill.associa.system.association.dto;

import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class AssociationResponseDto {
    Long id;
    String name;
    String slug;
    String description;
    String logoPath;
    String contactEmail;
    String contactPhone;
    String address;
    AssociationType type;
    AssociationStatus status;
    BigDecimal defaultMembershipFee;
    Integer membershipValidityMonths;
    Boolean financeApprovalWorkflow;
    Boolean autoApproveMembers;
    Integer foundedYear;
    String website;
    Long createdById;
    String createdByName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}