package bf.kvill.associa.votes.dto;

import bf.kvill.associa.votes.VoteStatus;
import bf.kvill.associa.votes.VoteType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VoteResponse {
    private Long id;
    private String title;
    private String description;
    private VoteStatus status;
    private Integer quorum;
    private Integer majority;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private VoteType type;
    private long totalVotes;
    private boolean userHasVoted;
    private List<VoteOptionResponse> options;
}
