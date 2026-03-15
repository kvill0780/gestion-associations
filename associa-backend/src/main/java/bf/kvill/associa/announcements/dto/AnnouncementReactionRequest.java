package bf.kvill.associa.announcements.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementReactionRequest {
    @NotBlank(message = "Le type de réaction est obligatoire")
    private String type;
}
