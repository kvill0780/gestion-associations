package bf.kvill.associa.system.association.dto;

import bf.kvill.associa.shared.enums.AssociationType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAssociationRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @NotBlank(message = "L'email de contact est obligatoire")
    @Email(message = "Email invalide")
    @Size(max = 100)
    private String contactEmail;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$", message = "Numéro de téléphone invalide")
    private String contactPhone;

    @Size(max = 255)
    private String address;

    @NotNull(message = "Le type d'association est obligatoire")
    private AssociationType type;

    @DecimalMin(value = "0.0", inclusive = false, message = "La cotisation doit être positive")
    @Digits(integer = 8, fraction = 2, message = "Montant invalide")
    private BigDecimal defaultMembershipFee;

    @Min(value = 1, message = "La durée de validité doit être au moins 1 mois")
    @Max(value = 60, message = "La durée de validité ne peut pas dépasser 60 mois")
    private Integer membershipValidityMonths;

    private Boolean financeApprovalWorkflow;

    private Boolean autoApproveMembers;

    @Min(value = 1900, message = "Année invalide")
    @Max(value = 2100, message = "Année invalide")
    private Integer foundedYear;

    @Pattern(regexp = "^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*$", message = "URL invalide")
    private String website;
}