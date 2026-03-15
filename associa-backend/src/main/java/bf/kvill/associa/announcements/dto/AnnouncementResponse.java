package bf.kvill.associa.announcements.dto;

import bf.kvill.associa.announcements.AnnouncementPriority;
import bf.kvill.associa.announcements.AnnouncementType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private AnnouncementPriority priority;
    private AnnouncementType type;
    private String pollQuestion;
    private Boolean allowMultipleVotes;
    private Long likesCount;
    private Long dislikesCount;
    private String userReaction;
    private String authorName;
    private List<AnnouncementPollOptionResponse> pollOptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
