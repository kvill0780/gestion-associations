package bf.kvill.associa.votes.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VoteResultsResponse {
    private double participationRate;
    private long totalVotes;
    private long totalMembers;
    private boolean quorumReached;
    private VoteSummary vote;
    private List<OptionResult> options;

    @Data
    @Builder
    public static class VoteSummary {
        private Integer quorum;
        private Integer majority;
    }

    @Data
    @Builder
    public static class OptionResult {
        private Long id;
        private String text;
        private long votes;
        private double percentage;
    }
}
