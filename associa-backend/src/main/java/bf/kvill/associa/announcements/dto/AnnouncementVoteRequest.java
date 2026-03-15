package bf.kvill.associa.announcements.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnnouncementVoteRequest {
    @NotNull(message = "OptionId requis")
    private Long optionId;
}
