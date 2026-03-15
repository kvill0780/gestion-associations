package bf.kvill.associa.security.auth.dto;

import lombok.Data;

@Data
public class LogoutRequest {

    private String refreshToken;

    /**
     * Si true, révoque tous les tokens de l'utilisateur
     */
    private Boolean revokeAllTokens = false;
}