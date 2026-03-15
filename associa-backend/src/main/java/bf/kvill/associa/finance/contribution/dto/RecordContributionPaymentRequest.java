package bf.kvill.associa.finance.contribution.dto;

import bf.kvill.associa.shared.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RecordContributionPaymentRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit etre strictement positif")
    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private LocalDate transactionDate;

    private String notes;
}
