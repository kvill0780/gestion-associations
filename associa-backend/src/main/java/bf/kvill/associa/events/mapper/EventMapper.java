package bf.kvill.associa.events.mapper;

import bf.kvill.associa.events.Event;
import bf.kvill.associa.events.dto.EventRequest;
import bf.kvill.associa.events.dto.EventResponse;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.system.association.Association;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request, Association association, User creator) {
        if (request == null) {
            return null;
        }

        return Event.builder()
                .association(association)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .status(request.getStatus()) // Peut être null, géré par PrePersist
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .isOnline(request.isOnline())
                .meetingLink(request.getMeetingLink())
                .maxParticipants(request.getMaxParticipants())
                .createdBy(creator)
                .build();
    }

    public void updateEntity(Event event, EventRequest request) {
        if (request == null || event == null)
            return;

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setType(request.getType());
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setLocation(request.getLocation());
        event.setOnline(request.isOnline());
        event.setMeetingLink(request.getMeetingLink());
        event.setMaxParticipants(request.getMaxParticipants());
    }

    public EventResponse toResponseDto(Event event) {
        if (event == null)
            return null;

        return EventResponse.builder()
                .id(event.getId())
                .associationId(event.getAssociation() != null ? event.getAssociation().getId() : null)
                .title(event.getTitle())
                .description(event.getDescription())
                .type(event.getType())
                .status(event.getStatus())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .isOnline(event.isOnline())
                .meetingLink(event.getMeetingLink())
                .maxParticipants(event.getMaxParticipants())
                .createdByName(event.getCreatedBy() != null ? event.getCreatedBy().getFullName() : null)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
