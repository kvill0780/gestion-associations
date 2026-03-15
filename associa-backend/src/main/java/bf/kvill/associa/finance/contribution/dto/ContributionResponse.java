package bf.kvill.associa.finance.contribution.dto;

import bf.kvill.associa.shared.enums.ContributionStatus;
import bf.kvill.associa.shared.enums.ContributionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributionResponse {
    private Long id;
    private Long associationId;
    private Long memberId;
    private String memberName;
    private Integer year;
    private Integer month;
    private String periodLabel;
    private ContributionType type;
    private BigDecimal expectedAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private ContributionStatus status;
    private LocalDate dueDate;
    private LocalDate firstPaymentDate;
    private LocalDate lastPaymentDate;
    private Boolean waived;
    private String waivedReason;
    private String notes;
    private List<ContributionPaymentResponse> payments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
