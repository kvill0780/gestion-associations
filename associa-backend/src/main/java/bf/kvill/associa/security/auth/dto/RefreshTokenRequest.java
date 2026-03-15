package bf.kvill.associa.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * Si true, génère un nouveau refresh token (rotation)
     */
    private Boolean rotateRefreshToken = false;
}