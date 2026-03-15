package bf.kvill.associa.system.association.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class ExecutiveBoardMemberDto {
    Long postId;
    String postName;
    Long userId;
    String userFullName;
    String userEmail;
    LocalDate startDate;
    LocalDate endDate;
    Boolean vacant;
}