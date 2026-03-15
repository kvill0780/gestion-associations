package bf.kvill.associa.announcements.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnouncementPollOptionResponse {
    private Long id;
    private String optionText;
    private long votesCount;
    private boolean userVoted;
}
