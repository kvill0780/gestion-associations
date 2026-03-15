package bf.kvill.associa.finance.contribution.dto;

import bf.kvill.associa.shared.enums.ContributionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateContributionRequest {

    @NotNull(message = "Le membre est obligatoire")
    private Long memberId;

    @NotNull(message = "L'annee est obligatoire")
    private Integer year;

    @Min(value = 1, message = "Le mois doit etre entre 1 et 12")
    @Max(value = 12, message = "Le mois doit etre entre 1 et 12")
    private Integer month;

    private ContributionType type;

    @NotNull(message = "Le montant attendu est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit etre strictement positif")
    private BigDecimal expectedAmount;

    private LocalDate dueDate;

    private Boolean waived;

    private String waivedReason;

    private String notes;
}
