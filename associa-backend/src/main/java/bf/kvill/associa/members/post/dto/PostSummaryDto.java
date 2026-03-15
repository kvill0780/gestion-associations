package bf.kvill.associa.members.post.dto;

import lombok.Builder;
import lombok.Value;

/**
 * DTO résumé d'un poste (pour listes)
 */
@Value
@Builder
public class PostSummaryDto {
    Long id;
    String name;
    Boolean isExecutive;
    Boolean isActive;
    Integer maxOccupants;
    Boolean canAcceptNewMandate;
}