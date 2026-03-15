package bf.kvill.associa.finance.transaction.dto;

import bf.kvill.associa.shared.enums.PaymentMethod;
import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private Long associationId;
    private Long userId;
    private Long contributionId;
    private Long recordedById;
    private Long validatedById;
    private TransactionType type;
    private TransactionCategory category;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String academicYear;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String notes;
    private LocalDateTime validatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
