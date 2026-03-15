package bf.kvill.associa.messages;

import bf.kvill.associa.messages.dto.ConversationResponse;
import bf.kvill.associa.messages.dto.MessageResponse;
import bf.kvill.associa.messages.dto.MessageSendRequest;
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
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Messagerie interne")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Lister les conversations")
    @GetMapping
    @PreAuthorize("hasPermission(null, 'messages.view')")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(
                messageService.getConversations(principal.getAssociationId(), principal.getId()));
    }

    @Operation(summary = "Récupérer une conversation")
    @GetMapping("/conversation/{userId}")
    @PreAuthorize("hasPermission(null, 'messages.view')")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(
                messageService.getConversation(principal.getAssociationId(), principal.getId(), userId));
    }

    @Operation(summary = "Envoyer un message")
    @PostMapping
    @PreAuthorize("hasPermission(null, 'messages.send')")
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageSendRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        MessageResponse response = messageService.sendMessage(
                principal.getAssociationId(), principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Nombre de messages non lus")
    @GetMapping("/unread-count")
    @PreAuthorize("hasPermission(null, 'messages.view')")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        long count = messageService.getUnreadCount(principal.getAssociationId(), principal.getId());
        return ResponseEntity.ok(count);
    }
}
