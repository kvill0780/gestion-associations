package bf.kvill.associa.votes.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoteOptionResponse {
    private Long id;
    private String optionText;
}
