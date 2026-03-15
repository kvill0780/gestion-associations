package bf.kvill.associa.events.dto;

import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.enums.EventType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    private String description;

    @NotNull(message = "Le type d'événement est obligatoire")
    private EventType type;

    private EventStatus status;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDateTime endDate;

    private String location;

    private boolean isOnline;

    private String meetingLink;

    private Integer maxParticipants;
}
