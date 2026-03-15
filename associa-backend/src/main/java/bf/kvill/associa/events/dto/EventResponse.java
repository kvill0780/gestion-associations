package bf.kvill.associa.events.dto;

import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EventResponse {
    private Long id;
    private Long associationId;
    private String title;
    private String description;
    private EventType type;
    private EventStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private boolean isOnline;
    private String meetingLink;
    private Integer maxParticipants;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
