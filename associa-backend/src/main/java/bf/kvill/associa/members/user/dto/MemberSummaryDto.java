package bf.kvill.associa.members.user.dto;

import bf.kvill.associa.shared.enums.MembershipStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MemberSummaryDto {
    Long id;
    String fullName;
    String email;
    MembershipStatus membershipStatus;
    LocalDate membershipDate;
    String profilePicturePath;
}