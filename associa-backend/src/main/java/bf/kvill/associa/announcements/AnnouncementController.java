package bf.kvill.associa.announcements;

import bf.kvill.associa.announcements.dto.AnnouncementReactionRequest;
import bf.kvill.associa.announcements.dto.AnnouncementRequest;
import bf.kvill.associa.announcements.dto.AnnouncementResponse;
import bf.kvill.associa.announcements.dto.AnnouncementVoteRequest;
import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Tag(name = "Annonces", description = "API des annonces et sondages")
@SecurityRequirement(name = "bearerAuth")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "Lister les annonces")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'announcements.view')")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(
                announcementService.getAnnouncements(principal.getAssociationId(), principal.getId()));
    }

    @Operation(summary = "Créer une annonce")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'announcements.create')")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AnnouncementResponse created = announcementService.createAnnouncement(
                request,
                principal.getAssociationId(),
                principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Supprimer une annonce")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'announcements.delete')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        announcementService.deleteAnnouncement(id, principal.getAssociationId(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Réagir à une annonce")
    @PostMapping("/{id}/react")
    @PreAuthorize("hasPermission(null, 'announcements.view')")
    public ResponseEntity<Void> react(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementReactionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AnnouncementReactionType type = AnnouncementReactionType.valueOf(request.getType().toUpperCase());
        announcementService.react(id, principal.getAssociationId(), principal.getId(), type);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Retirer une réaction")
    @DeleteMapping("/{id}/react")
    @PreAuthorize("hasPermission(null, 'announcements.view')")
    public ResponseEntity<Void> unreact(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        announcementService.unreact(id, principal.getAssociationId(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Voter pour un sondage")
    @PostMapping("/{id}/vote")
    @PreAuthorize("hasPermission(null, 'announcements.view')")
    public ResponseEntity<Void> vote(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementVoteRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        announcementService.vote(id, principal.getAssociationId(), principal.getId(), request.getOptionId());
        return ResponseEntity.ok().build();
    }
}
