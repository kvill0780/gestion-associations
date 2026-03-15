package bf.kvill.associa.events;

import bf.kvill.associa.events.dto.EventRequest;
import bf.kvill.associa.events.dto.EventResponse;
import bf.kvill.associa.events.enums.EventStatus;
import bf.kvill.associa.events.participation.EventParticipationService;
import bf.kvill.associa.events.participation.dto.EventAttendanceSummaryResponse;
import bf.kvill.associa.events.participation.dto.EventParticipantResponse;
import bf.kvill.associa.events.participation.dto.RegisterParticipantRequest;
import bf.kvill.associa.events.participation.dto.UpdateParticipationStatusRequest;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Événements", description = "API de gestion des événements (réunions, formations, etc.)")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;
    private final EventParticipationService eventParticipationService;

    @Operation(summary = "Lister les événements", description = "Récupère la liste paginée des événements de l'association")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'events.view')")
    public ResponseEntity<Page<EventResponse>> getEvents(
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EventResponse> events = eventService.getEventsByAssociation(currentUser.getAssociationId(), pageable);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Récupérer un événement", description = "Récupère les détails d'un événement par son ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'events.view')")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventResponse event = eventService.getEventById(id, currentUser.getAssociationId());
        return ResponseEntity.ok(event);
    }

    @Operation(summary = "Créer un événement", description = "Crée un nouvel événement (nécessite events.manage)")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'events.manage')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventResponse createdEvent = eventService.createEvent(request, currentUser.getAssociationId(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @Operation(summary = "Modifier un événement", description = "Met à jour un événement existant (nécessite events.manage)")
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'events.manage')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventResponse updatedEvent = eventService.updateEvent(id, request, currentUser.getAssociationId(),
                currentUser.getId());
        return ResponseEntity.ok(updatedEvent);
    }

    @Operation(summary = "Modifier le statut d'un événement", description = "Change le statut (DRAFT, PUBLISHED, etc.) d'un événement")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasPermission(null, 'events.manage')")
    public ResponseEntity<EventResponse> changeEventStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventResponse updatedEvent = eventService.changeEventStatus(id, status, currentUser.getAssociationId(),
                currentUser.getId());
        return ResponseEntity.ok(updatedEvent);
    }


    @Operation(summary = "Lister les participants d'un événement", description = "Récupère la liste des participants et leur statut")
    @GetMapping("/{id}/participants")
    @PreAuthorize("hasPermission(null, 'events.view')")
    public ResponseEntity<java.util.List<EventParticipantResponse>> getParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        java.util.List<EventParticipantResponse> participants =
                eventParticipationService.getParticipants(id, currentUser.getAssociationId());
        return ResponseEntity.ok(participants);
    }

    @Operation(summary = "Récupérer le résumé de présence", description = "Retourne les statistiques de participation de l'événement")
    @GetMapping("/{id}/attendance-summary")
    @PreAuthorize("hasPermission(null, 'events.view')")
    public ResponseEntity<EventAttendanceSummaryResponse> getAttendanceSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventAttendanceSummaryResponse summary =
                eventParticipationService.getAttendanceSummary(id, currentUser.getAssociationId());
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Inscrire un participant", description = "Inscrit un membre à un événement (nécessite events.manage)")
    @PostMapping("/{id}/participants/register")
    @PreAuthorize("hasPermission(null, 'events.manage')")
    public ResponseEntity<EventParticipantResponse> registerParticipant(
            @PathVariable Long id,
            @Valid @RequestBody RegisterParticipantRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventParticipantResponse response = eventParticipationService.registerParticipant(
                id,
                request.getUserId(),
                request.getNotes(),
                currentUser.getId(),
                currentUser.getAssociationId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check-in personnel", description = "Marque l'utilisateur connecté comme présent")
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasPermission(null, 'events.view')")
    public ResponseEntity<EventParticipantResponse> checkInSelf(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventParticipantResponse response = eventParticipationService.checkInSelf(
                id,
                currentUser.getId(),
                currentUser.getAssociationId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mettre à jour le statut d'un participant", description = "Met à jour le statut de présence d'un participant")
    @PatchMapping("/{id}/participants/{userId}/status")
    @PreAuthorize("hasPermission(null, 'events.manage')")
    public ResponseEntity<EventParticipantResponse> updateParticipantStatus(
            @PathVariable Long id,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateParticipationStatusRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        EventParticipantResponse response = eventParticipationService.updateParticipantStatus(
                id,
                userId,
                request.getStatus(),
                request.getNotes(),
                currentUser.getId(),
                currentUser.getAssociationId());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Supprimer un événement", description = "Supprime logiciellement un événement (nécessite events.delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'events.delete')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal currentUser) {

        eventService.deleteEvent(id, currentUser.getAssociationId(), currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
