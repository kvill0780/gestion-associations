package bf.kvill.associa.events.participation;

import bf.kvill.associa.events.Event;
import bf.kvill.associa.events.EventRepository;
import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.participation.dto.EventAttendanceSummaryResponse;
import bf.kvill.associa.events.participation.dto.EventParticipantResponse;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.EventParticipationStatus;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.exception.BusinessException;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventParticipationService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private static final EnumSet<EventParticipationStatus> OCCUPYING_STATUSES =
            EnumSet.of(EventParticipationStatus.REGISTERED, EventParticipationStatus.ATTENDED);

    @Transactional(readOnly = true)
    public List<EventParticipantResponse> getParticipants(Long eventId, Long associationId) {
        Event event = getEventInAssociation(eventId, associationId);

        return participationRepository.findByEventIdOrderByRegisteredAtDesc(event.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventAttendanceSummaryResponse getAttendanceSummary(Long eventId, Long associationId) {
        Event event = getEventInAssociation(eventId, associationId);

        long total = participationRepository.countByEventId(event.getId());
        long registered = participationRepository.countByEventIdAndStatus(event.getId(), EventParticipationStatus.REGISTERED);
        long attended = participationRepository.countByEventIdAndStatus(event.getId(), EventParticipationStatus.ATTENDED);
        long absent = participationRepository.countByEventIdAndStatus(event.getId(), EventParticipationStatus.ABSENT);
        long cancelled = participationRepository.countByEventIdAndStatus(event.getId(), EventParticipationStatus.CANCELLED);

        Integer maxParticipants = event.getMaxParticipants();
        Integer availableSlots = null;
        if (maxParticipants != null) {
            long occupying = registered + attended;
            availableSlots = Math.max(0, maxParticipants - (int) occupying);
        }

        return EventAttendanceSummaryResponse.builder()
                .eventId(eventId)
                .maxParticipants(maxParticipants)
                .availableSlots(availableSlots)
                .totalParticipants(total)
                .registeredCount(registered)
                .attendedCount(attended)
                .absentCount(absent)
                .cancelledCount(cancelled)
                .build();
    }

    @Transactional
    public EventParticipantResponse registerParticipant(Long eventId, Long userId, String notes, Long actorId, Long associationId) {
        Event event = getEventInAssociation(eventId, associationId);
        User actor = findUser(actorId);
        ensureActorCanManageAssociation(actor, associationId);
        User participant = findParticipantInAssociation(userId, associationId);

        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessException("Impossible d'inscrire un participant à un événement annulé");
        }

        EventParticipation participation = participationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> {
                    EventParticipation ep = new EventParticipation();
                    ep.setEvent(event);
                    ep.setUser(participant);
                    ep.setRegisteredAt(LocalDateTime.now());
                    return ep;
                });

        EventParticipationStatus previousStatus = participation.getStatus();
        if (previousStatus == null) {
            previousStatus = EventParticipationStatus.REGISTERED;
        }

        ensureCapacity(event, previousStatus, EventParticipationStatus.REGISTERED);

        participation.setStatus(EventParticipationStatus.REGISTERED);
        if (notes != null) {
            participation.setNotes(notes);
        }

        EventParticipation saved = participationRepository.save(participation);

        auditService.log(
                "REGISTER_EVENT_PARTICIPANT",
                "EventParticipation",
                saved.getId(),
                actorId,
                Map.of("eventId", eventId, "userId", userId));

        return toResponse(saved);
    }

    @Transactional
    public EventParticipantResponse checkInSelf(Long eventId, Long currentUserId, Long associationId) {
        Event event = getEventInAssociation(eventId, associationId);
        User participant = findParticipantInAssociation(currentUserId, associationId);

        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.DRAFT) {
            throw new BusinessException("Le check-in n'est pas disponible pour cet événement");
        }

        EventParticipation participation = participationRepository.findByEventIdAndUserId(eventId, currentUserId)
                .orElseGet(() -> {
                    EventParticipation ep = new EventParticipation();
                    ep.setEvent(event);
                    ep.setUser(participant);
                    ep.setRegisteredAt(LocalDateTime.now());
                    return ep;
                });

        EventParticipationStatus previousStatus = participation.getStatus();
        if (previousStatus == null) {
            previousStatus = EventParticipationStatus.REGISTERED;
        }

        ensureCapacity(event, previousStatus, EventParticipationStatus.ATTENDED);

        participation.setStatus(EventParticipationStatus.ATTENDED);
        participation.setNotes("Check-in utilisateur");

        EventParticipation saved = participationRepository.save(participation);

        auditService.log(
                "CHECKIN_EVENT_PARTICIPANT",
                "EventParticipation",
                saved.getId(),
                currentUserId,
                Map.of("eventId", eventId, "userId", currentUserId));

        return toResponse(saved);
    }

    @Transactional
    public EventParticipantResponse updateParticipantStatus(
            Long eventId,
            Long userId,
            EventParticipationStatus newStatus,
            String notes,
            Long actorId,
            Long associationId) {

        Event event = getEventInAssociation(eventId, associationId);
        User actor = findUser(actorId);
        ensureActorCanManageAssociation(actor, associationId);
        User participant = findParticipantInAssociation(userId, associationId);

        EventParticipation participation = participationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseGet(() -> {
                    EventParticipation ep = new EventParticipation();
                    ep.setEvent(event);
                    ep.setUser(participant);
                    ep.setRegisteredAt(LocalDateTime.now());
                    return ep;
                });

        EventParticipationStatus previousStatus = participation.getStatus();
        if (previousStatus == null) {
            previousStatus = EventParticipationStatus.REGISTERED;
        }

        ensureCapacity(event, previousStatus, newStatus);

        participation.setStatus(newStatus);
        if (notes != null) {
            participation.setNotes(notes);
        }

        EventParticipation saved = participationRepository.save(participation);

        auditService.log(
                "UPDATE_EVENT_PARTICIPATION_STATUS",
                "EventParticipation",
                saved.getId(),
                actorId,
                Map.of("eventId", eventId, "userId", userId, "status", newStatus.name()));

        return toResponse(saved);
    }

    private Event getEventInAssociation(Long eventId, Long associationId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        Long eventAssociationId = event.getAssociation() != null ? event.getAssociation().getId() : null;
        if (eventAssociationId == null || !eventAssociationId.equals(associationId)) {
            throw new ResourceNotFoundException("Event", eventId);
        }

        return event;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private User findParticipantInAssociation(Long userId, Long associationId) {
        User user = findUser(userId);
        Long userAssociationId = user.getAssociation() != null ? user.getAssociation().getId() : null;

        if (userAssociationId == null || !userAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Participant hors de votre association");
        }

        if (user.getMembershipStatus() != MembershipStatus.ACTIVE) {
            throw new BusinessException("Seuls les membres actifs peuvent participer à un événement");
        }

        return user;
    }

    private void ensureActorCanManageAssociation(User actor, Long associationId) {
        if (actor.isSuperAdmin()) {
            return;
        }

        Long actorAssociationId = actor.getAssociation() != null ? actor.getAssociation().getId() : null;
        if (actorAssociationId == null || !actorAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }

    private void ensureCapacity(Event event, EventParticipationStatus oldStatus, EventParticipationStatus newStatus) {
        if (event.getMaxParticipants() == null || event.getMaxParticipants() <= 0) {
            return;
        }

        boolean oldOccupies = OCCUPYING_STATUSES.contains(oldStatus);
        boolean newOccupies = OCCUPYING_STATUSES.contains(newStatus);

        if (oldOccupies || !newOccupies) {
            return;
        }

        long occupyingCount = participationRepository.countByEventIdAndStatusIn(event.getId(), OCCUPYING_STATUSES);
        if (occupyingCount >= event.getMaxParticipants()) {
            throw new BusinessException("Capacité maximale atteinte pour cet événement");
        }
    }

    private EventParticipantResponse toResponse(EventParticipation participation) {
        return EventParticipantResponse.builder()
                .eventId(participation.getEvent() != null ? participation.getEvent().getId() : null)
                .userId(participation.getUser() != null ? participation.getUser().getId() : null)
                .userFullName(participation.getUser() != null ? participation.getUser().getFullName() : null)
                .userEmail(participation.getUser() != null ? participation.getUser().getEmail() : null)
                .userWhatsapp(participation.getUser() != null ? participation.getUser().getWhatsapp() : null)
                .status(participation.getStatus())
                .notes(participation.getNotes())
                .registeredAt(participation.getRegisteredAt())
                .updatedAt(participation.getUpdatedAt())
                .build();
    }
}
