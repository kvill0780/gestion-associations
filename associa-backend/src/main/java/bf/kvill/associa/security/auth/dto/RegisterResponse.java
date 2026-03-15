package bf.kvill.associa.security.auth.dto;

import bf.kvill.associa.shared.enums.MembershipStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterResponse {
    String message;
    Long userId;
    String email;
    MembershipStatus membershipStatus;
}