package bf.kvill.associa.events.participation.dto;

import bf.kvill.associa.shared.enums.EventParticipationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateParticipationStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private EventParticipationStatus status;

    private String notes;
}
