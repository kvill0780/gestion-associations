package bf.kvill.associa.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityDto {
    private String description;
    private String entityType;
    private String action;
    private LocalDateTime createdAt;
    private String userName;
    private String entityId;

    public ActivityDto() {
    }

    public ActivityDto(String description, String entityType, String action, LocalDateTime createdAt, String userName,
            String entityId) {
        this.description = description;
        this.entityType = entityType;
        this.action = action;
        this.createdAt = createdAt;
        this.userName = userName;
        this.entityId = entityId;
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static ActivityDtoBuilder builder() {
        return new ActivityDtoBuilder();
    }

    public static class ActivityDtoBuilder {
        private String description;
        private String entityType;
        private String action;
        private LocalDateTime createdAt;
        private String userName;
        private String entityId;

        ActivityDtoBuilder() {
        }

        public ActivityDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ActivityDtoBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public ActivityDtoBuilder action(String action) {
            this.action = action;
            return this;
        }

        public ActivityDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ActivityDtoBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public ActivityDtoBuilder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public ActivityDto build() {
            ActivityDto dto = new ActivityDto();
            dto.setDescription(description);
            dto.setEntityType(entityType);
            dto.setAction(action);
            dto.setCreatedAt(createdAt);
            dto.setUserName(userName);
            dto.setEntityId(entityId);
            return dto;
        }
    }
}
