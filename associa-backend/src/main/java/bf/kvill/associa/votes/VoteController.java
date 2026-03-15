package bf.kvill.associa.votes;

import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.votes.dto.CreateVoteRequest;
import bf.kvill.associa.votes.dto.VoteCastRequest;
import bf.kvill.associa.votes.dto.VoteResponse;
import bf.kvill.associa.votes.dto.VoteResultsResponse;
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
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Tag(name = "Votes", description = "Votes et décisions")
@SecurityRequirement(name = "bearerAuth")
public class VoteController {

    private final VoteService voteService;

    @Operation(summary = "Lister les votes")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'votes.view')")
    public ResponseEntity<List<VoteResponse>> getVotes(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(voteService.getVotes(principal.getAssociationId(), principal.getId()));
    }

    @Operation(summary = "Créer un vote")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'votes.create')")
    public ResponseEntity<VoteResponse> createVote(
            @Valid @RequestBody CreateVoteRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        VoteResponse created = voteService.createVote(request, principal.getAssociationId(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Voter")
    @PostMapping("/{id}/cast")
    @PreAuthorize("hasPermission(null, 'votes.cast')")
    public ResponseEntity<Void> castVote(
            @PathVariable Long id,
            @Valid @RequestBody VoteCastRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        voteService.castVote(id, principal.getAssociationId(), principal.getId(), request.getOptionId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Publier un vote")
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasPermission(null, 'votes.manage')")
    public ResponseEntity<Void> publishVote(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        voteService.publishVote(id, principal.getAssociationId(), principal.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Clôturer un vote")
    @PostMapping("/{id}/close")
    @PreAuthorize("hasPermission(null, 'votes.manage')")
    public ResponseEntity<Void> closeVote(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        voteService.closeVote(id, principal.getAssociationId(), principal.getId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Résultats du vote")
    @GetMapping("/{id}/results")
    @PreAuthorize("hasPermission(null, 'votes.view')")
    public ResponseEntity<VoteResultsResponse> getResults(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(voteService.getResults(id, principal.getAssociationId()));
    }
}
