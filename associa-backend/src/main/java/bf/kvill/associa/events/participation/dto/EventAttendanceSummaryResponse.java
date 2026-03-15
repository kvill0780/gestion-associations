package bf.kvill.associa.events.participation.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EventAttendanceSummaryResponse {
    Long eventId;
    Integer maxParticipants;
    Integer availableSlots;
    long totalParticipants;
    long registeredCount;
    long attendedCount;
    long absentCount;
    long cancelledCount;
}
