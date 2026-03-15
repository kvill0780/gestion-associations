package bf.kvill.associa.finance.transaction.dto;

import bf.kvill.associa.shared.enums.PaymentMethod;
import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateTransactionRequest {

    @NotNull(message = "Le type est obligatoire")
    private TransactionType type;

    @NotNull(message = "La catégorie est obligatoire")
    private TransactionCategory category;

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être strictement positif")
    private BigDecimal amount;

    @NotNull(message = "La date de transaction est obligatoire")
    private LocalDate transactionDate;

    private String academicYear;

    private PaymentMethod paymentMethod;

    private String notes;

    private Long userId;
}
