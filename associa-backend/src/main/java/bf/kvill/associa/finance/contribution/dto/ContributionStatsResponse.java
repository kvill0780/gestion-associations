package bf.kvill.associa.finance.contribution.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributionStatsResponse {
    private long totalMembers;
    private long upToDate;
    private long late;
    private long partial;
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private double collectionRate;
}
