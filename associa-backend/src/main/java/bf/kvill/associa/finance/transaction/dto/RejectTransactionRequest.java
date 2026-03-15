package bf.kvill.associa.finance.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectTransactionRequest {

    @NotBlank(message = "La raison du rejet est obligatoire")
    private String reason;
}
