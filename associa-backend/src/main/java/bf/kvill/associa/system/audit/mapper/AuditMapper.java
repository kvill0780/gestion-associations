package bf.kvill.associa.system.audit.mapper;

import bf.kvill.associa.system.audit.AuditLog;
import bf.kvill.associa.system.audit.dto.AuditLogDto;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir AuditLog ↔ DTO
 */
@Component
public class AuditMapper {

    public AuditLogDto toDto(AuditLog auditLog) {
        if (auditLog == null) return null;

        return AuditLogDto.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .userId(auditLog.getUserId())
                .userName(auditLog.getUserName())
                .userEmail(auditLog.getUserEmail())
                .associationId(auditLog.getAssociationId())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .metadata(auditLog.getMetadata())
                .description(auditLog.getDescription())
                .severity(auditLog.getSeverity())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}