package bf.kvill.associa.finance.contribution.dto;

import bf.kvill.associa.shared.enums.PaymentMethod;
import bf.kvill.associa.shared.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContributionPaymentResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private String notes;
}
