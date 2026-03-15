package bf.kvill.associa.announcements.dto;

import bf.kvill.associa.announcements.AnnouncementPriority;
import bf.kvill.associa.announcements.AnnouncementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AnnouncementRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "Le contenu est obligatoire")
    private String content;

    @NotNull(message = "La priorité est obligatoire")
    private AnnouncementPriority priority;

    @NotNull(message = "Le type est obligatoire")
    private AnnouncementType type;

    private String pollQuestion;

    @Size(max = 20, message = "Trop d'options")
    private List<String> pollOptions;

    private Boolean allowMultipleVotes;
}
