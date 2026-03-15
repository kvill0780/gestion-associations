package bf.kvill.associa.members.post.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de requête pour modifier un poste
 */
@Data
public class UpdatePostRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    private Boolean isExecutive;

    @Min(1)
    @Max(100)
    private Integer maxOccupants;

    @Min(0)
    private Integer displayOrder;

    @Min(1)
    @Max(120)
    private Integer mandateDurationMonths;

    private Boolean requiresElection;

    /**
     * Rôle par défaut explicite du poste (optionnel)
     */
    private Long defaultRoleId;

    /**
     * Permet de retirer explicitement le rôle par défaut.
     */
    private Boolean clearDefaultRole;

    // Getters explicites pour Boolean (Lombok génère isXxx() au lieu de getIsXxx())
    public Boolean getIsExecutive() {
        return isExecutive;
    }

    public Boolean getRequiresElection() {
        return requiresElection;
    }
}
