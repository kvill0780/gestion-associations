package bf.kvill.associa.events;

import bf.kvill.associa.events.dto.EventRequest;
import bf.kvill.associa.events.dto.EventResponse;
import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.mapper.EventMapper;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import bf.kvill.associa.shared.exception.BusinessException;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuditService auditService;
    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;

    @Transactional(readOnly = true)
    public Page<EventResponse> getEventsByAssociation(Long associationId, Pageable pageable) {
        log.debug("Récupération des événements pour l'association {}", associationId);
        return eventRepository.findByAssociationId(associationId, pageable)
                .map(eventMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public EventResponse getEventById(Long eventId, Long associationId) {
        log.debug("Récupération de l'événement {}", eventId);
        Event event = findAndVerifyEvent(eventId, associationId);
        return eventMapper.toResponseDto(event);
    }

    @Transactional
    public EventResponse createEvent(EventRequest request, Long associationId, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));
        verifyActorInAssociation(creator, associationId);
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));

        log.info("Création d'un nouvel événement '{}' par {}", request.getTitle(), creator.getId());

        validateDates(request);

        Event event = eventMapper.toEntity(request, association, creator);
        event = eventRepository.save(event);

        auditService.log(
                "CREATE_EVENT",
                "Event",
                event.getId(),
                creator,
                Map.of("title", event.getTitle(), "type", event.getType().name()));

        return eventMapper.toResponseDto(event);
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long associationId, Long updaterId) {
        log.info("Mise à jour de l'événement {} par {}", eventId, updaterId);
        verifyActorInAssociation(updaterId, associationId);

        Event event = findAndVerifyEvent(eventId, associationId);
        validateDates(request);

        eventMapper.updateEntity(event, request);
        event = eventRepository.save(event);

        auditService.log(
                "UPDATE_EVENT",
                "Event",
                event.getId(),
                updaterId,
                Map.of("title", event.getTitle(), "status", event.getStatus().name()));

        return eventMapper.toResponseDto(event);
    }

    @Transactional
    public EventResponse changeEventStatus(Long eventId, EventStatus newStatus, Long associationId, Long updaterId) {
        log.info("Changement de statut de l'événement {} à {} par {}", eventId, newStatus, updaterId);
        verifyActorInAssociation(updaterId, associationId);

        Event event = findAndVerifyEvent(eventId, associationId);

        if (event.getStatus() == newStatus) {
            return eventMapper.toResponseDto(event); // Pas de changement
        }

        String oldStatus = event.getStatus().name();
        event.setStatus(newStatus);
        event = eventRepository.save(event);

        auditService.log(
                "CHANGE_EVENT_STATUS",
                "Event",
                event.getId(),
                updaterId,
                Map.of("oldStatus", oldStatus, "newStatus", newStatus.name()));

        return eventMapper.toResponseDto(event);
    }

    @Transactional
    public void deleteEvent(Long eventId, Long associationId, Long deleterId) {
        log.info("Suppression de l'événement {} par {}", eventId, deleterId);
        verifyActorInAssociation(deleterId, associationId);

        Event event = findAndVerifyEvent(eventId, associationId);

        // Soft delete handle via l'annotation @SQLDelete sur l'entité Entity
        eventRepository.delete(event);

        auditService.log(
                "DELETE_EVENT",
                "Event",
                eventId,
                deleterId,
                Map.of("title", event.getTitle()));
    }

    // ==================== MÉTHODES PRIVÉES UTILS ====================

    private Event findAndVerifyEvent(Long eventId, Long associationId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

        if (!event.getAssociation().getId().equals(associationId)) {
            // Pour des raisons de sécurité, on throw une 404 même s'il existe ailleurs pour
            // ne pas leak l'info
            log.warn("Tentative d'accès à l'événement {} de l'association {} depuis l'association {}",
                    eventId, event.getAssociation().getId(), associationId);
            throw new ResourceNotFoundException("Event", eventId);
        }

        return event;
    }

    private void validateDates(EventRequest request) {
        if (request.getEndDate() != null && request.getStartDate() != null) {
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new BusinessException("La date de fin ne peut pas être avant la date de début.");
            }
        }
    }

    private void verifyActorInAssociation(Long actorId, Long associationId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", actorId));
        verifyActorInAssociation(actor, associationId);
    }

    private void verifyActorInAssociation(User actor, Long associationId) {
        if (actor.isSuperAdmin()) {
            return;
        }
        Long actorAssociationId = actor.getAssociation() != null ? actor.getAssociation().getId() : null;
        if (actorAssociationId == null || !actorAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }
}
