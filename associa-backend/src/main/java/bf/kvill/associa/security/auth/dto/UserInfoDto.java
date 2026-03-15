package bf.kvill.associa.security.auth.dto;

import bf.kvill.associa.shared.enums.MembershipStatus;
import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class UserInfoDto {
    Long id;
    String email;
    String firstName;
    String lastName;
    String fullName;
    String phone;
    MembershipStatus membershipStatus;
    Boolean isSuperAdmin;
    List<String> roles;
    List<String> permissions;
    List<bf.kvill.associa.members.mandate.dto.MandateResponseDto> currentMandates;
    Long associationId;
    String associationName;
}