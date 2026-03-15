package bf.kvill.associa.messages;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.messages.dto.ConversationResponse;
import bf.kvill.associa.messages.dto.MessageResponse;
import bf.kvill.associa.messages.dto.MessageSendRequest;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long associationId, Long userId) {
        List<Message> messages = messageRepository.findAllForUser(associationId, userId);
        Map<Long, ConversationResponse.ConversationResponseBuilder> conversations = new LinkedHashMap<>();
        Map<Long, Long> unreadCounts = new LinkedHashMap<>();

        for (Message message : messages) {
            User other = message.getSender().getId().equals(userId) ? message.getReceiver() : message.getSender();
            Long otherId = other.getId();

            if (!conversations.containsKey(otherId)) {
                conversations.put(otherId, ConversationResponse.builder()
                        .user(ConversationResponse.UserSummary.builder()
                                .id(otherId)
                                .name(other.getFullName())
                                .email(other.getEmail())
                                .build())
                        .lastMessage(ConversationResponse.MessageSummary.builder()
                                .content(message.getContent())
                                .createdAt(message.getCreatedAt())
                                .build()));
                unreadCounts.put(otherId, 0L);
            }

            if (message.getReceiver().getId().equals(userId) && message.getReadAt() == null) {
                unreadCounts.put(otherId, unreadCounts.getOrDefault(otherId, 0L) + 1);
            }
        }

        List<ConversationResponse> result = new ArrayList<>();
        for (Map.Entry<Long, ConversationResponse.ConversationResponseBuilder> entry : conversations.entrySet()) {
            long unread = unreadCounts.getOrDefault(entry.getKey(), 0L);
            result.add(entry.getValue().unreadCount(unread).build());
        }
        return result;
    }

    @Transactional
    public List<MessageResponse> getConversation(Long associationId, Long userId, Long otherUserId) {
        List<Message> messages = messageRepository.findConversation(associationId, userId, otherUserId);
        List<MessageResponse> responses = new ArrayList<>();

        for (Message message : messages) {
            if (message.getReceiver().getId().equals(userId) && message.getReadAt() == null) {
                message.setReadAt(LocalDateTime.now());
            }
            responses.add(toResponse(message));
        }

        return responses;
    }

    @Transactional
    public MessageResponse sendMessage(Long associationId, Long senderId, MessageSendRequest request) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getReceiverId()));

        verifyActorInAssociation(sender, associationId);
        verifyActorInAssociation(receiver, associationId);

        Message message = Message.builder()
                .association(association)
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);

        auditService.log(
                "SEND_MESSAGE",
                "Message",
                saved.getId(),
                senderId,
                Map.of("receiverId", receiver.getId()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long associationId, Long userId) {
        return messageRepository.countByAssociationIdAndReceiverIdAndReadAtIsNull(associationId, userId);
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .receiverId(message.getReceiver() != null ? message.getReceiver().getId() : null)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .readAt(message.getReadAt())
                .build();
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
