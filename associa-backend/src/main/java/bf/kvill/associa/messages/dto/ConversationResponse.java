package bf.kvill.associa.messages.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private UserSummary user;
    private MessageSummary lastMessage;
    private long unreadCount;

    @Data
    @Builder
    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    public static class MessageSummary {
        private String content;
        private java.time.LocalDateTime createdAt;
    }
}
