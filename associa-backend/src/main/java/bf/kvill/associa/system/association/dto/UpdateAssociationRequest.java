package bf.kvill.associa.system.association.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateAssociationRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    private String logoPath;

    @Email
    @Size(max = 100)
    private String contactEmail;

    @Pattern(regexp = "^[+]?[0-9]{8,20}$")
    private String contactPhone;

    @Size(max = 255)
    private String address;

    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal defaultMembershipFee;

    @Min(1)
    @Max(60)
    private Integer membershipValidityMonths;

    private Boolean financeApprovalWorkflow;

    private Boolean autoApproveMembers;

    @Pattern(regexp = "^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*$")
    private String website;
}