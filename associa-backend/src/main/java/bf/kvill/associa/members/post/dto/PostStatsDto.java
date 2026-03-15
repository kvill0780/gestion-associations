package bf.kvill.associa.members.post.dto;

import lombok.Builder;
import lombok.Value;

/**
 * DTO pour les statistiques d'un poste
 */
@Value
@Builder
public class PostStatsDto {
    Long postId;
    String postName;
    Long totalMandates;        // Nombre total de mandats (historique)
    Long activeMandates;       // Nombre de mandats actifs
    Integer maxOccupants;      // Limite d'occupants
    Long availableSlots;       // Places disponibles
    Boolean canAcceptNewMandate;
    Boolean isExecutive;
    Boolean isActive;
    Integer suggestedRolesCount;
}
