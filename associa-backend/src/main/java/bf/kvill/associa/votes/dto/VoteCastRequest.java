package bf.kvill.associa.votes.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteCastRequest {
    @NotNull(message = "Option requise")
    private Long optionId;
}
