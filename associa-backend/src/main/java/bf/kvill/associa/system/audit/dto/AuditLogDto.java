package bf.kvill.associa.system.audit.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Map;

@Value
@Builder
public class AuditLogDto {
    Long id;
    String action;
    String entityType;
    Long entityId;
    Long userId;
    String userName;
    String userEmail;
    Long associationId;
    String ipAddress;
    String userAgent;
    Map<String, Object> metadata;
    String description;
    String severity;
    LocalDateTime createdAt;
}