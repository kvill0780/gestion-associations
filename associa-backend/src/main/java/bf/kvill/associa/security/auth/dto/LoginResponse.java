package bf.kvill.associa.security.auth.dto;

import bf.kvill.associa.security.auth.dto.UserInfoDto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
    UserInfoDto user;
}
