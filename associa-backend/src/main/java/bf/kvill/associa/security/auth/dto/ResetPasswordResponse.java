package bf.kvill.associa.security.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResetPasswordResponse {
    String message;
    String email;
}