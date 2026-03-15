package bf.kvill.associa.events.participation.dto;

import bf.kvill.associa.shared.enums.EventParticipationStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class EventParticipantResponse {
    Long eventId;
    Long userId;
    String userFullName;
    String userEmail;
    String userWhatsapp;
    EventParticipationStatus status;
    String notes;
    LocalDateTime registeredAt;
    LocalDateTime updatedAt;
}
