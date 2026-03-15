package bf.kvill.associa.members.post.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO de requête pour créer un poste
 */
@Data
public class CreatePostRequest {

    @NotBlank(message = "Le nom du poste est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    private Boolean isExecutive;

    @Min(value = 1, message = "Le nombre maximum d'occupants doit être au moins 1")
    @Max(value = 100, message = "Le nombre maximum d'occupants ne peut pas dépasser 100")
    private Integer maxOccupants;

    @Min(value = 0, message = "L'ordre d'affichage ne peut pas être négatif")
    private Integer displayOrder;

    @Min(value = 1, message = "La durée du mandat doit être au moins 1 mois")
    @Max(value = 120, message = "La durée du mandat ne peut pas dépasser 120 mois")
    private Integer mandateDurationMonths;

    private Boolean requiresElection;

    /**
     * Rôle par défaut explicite du poste (optionnel)
     */
    private Long defaultRoleId;

    @NotNull(message = "L'association est obligatoire")
    private Long associationId;
}
