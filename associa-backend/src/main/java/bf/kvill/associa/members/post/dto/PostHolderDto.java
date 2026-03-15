package bf.kvill.associa.members.post.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * DTO pour un titulaire actuel d'un poste
 */
@Value
@Builder
public class PostHolderDto {
    Long mandateId;
    Long userId;
    String userFullName;
    String userEmail;
    LocalDate startDate;
    LocalDate endDate;
}