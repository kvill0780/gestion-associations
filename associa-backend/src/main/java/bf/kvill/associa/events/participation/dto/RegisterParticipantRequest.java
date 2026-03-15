package bf.kvill.associa.events.participation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterParticipantRequest {

    @NotNull(message = "L'utilisateur est obligatoire")
    private Long userId;

    private String notes;
}
