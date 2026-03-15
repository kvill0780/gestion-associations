package bf.kvill.associa.system.audit;

import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour créer des logs d'audit
 * 
 * Utilisé partout dans l'application pour tracer les actions
 * 
 * Exemples d'utilisation :
 * 
 * auditService.log("CREATE_USER", "User", userId, currentUser.getId(),
 * Map.of("email", "jean@example.com"));
 * 
 * auditService.log("ASSIGN_POST", "Mandate", mandateId, currentUser.getId(),
 * Map.of("postName", "Président", "userName", "Jean Dupont"));
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditRepository auditRepository;
    private final UserRepository userRepository;

    /**
     * Crée un log d'audit complet
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, Long userId, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(userId)
                    .metadata(metadata)
                    .severity("INFO")
                    .build();

            enrichWithUserInfo(auditLog, userId);
            enrichAssociationFromMetadata(auditLog);

            // Enrichir avec infos HTTP si disponibles
            enrichWithHttpInfo(auditLog);

            auditRepository.save(auditLog);

            log.debug("Audit log créé: {} sur {} #{}", action, entityType, entityId);

        } catch (Exception e) {
            // Ne pas faire échouer l'action principale si l'audit échoue
            log.error("Erreur création audit log: {}", e.getMessage());
        }
    }

    /**
     * Crée un log d'audit avec utilisateur complet
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, User user, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(user != null ? user.getId() : null)
                    .userName(user != null ? user.getFullName() : null)
                    .userEmail(user != null ? user.getUsername() : null)
                    .associationId(user != null && user.getAssociation() != null
                            ? user.getAssociation().getId()
                            : null)
                    .metadata(metadata)
                    .severity("INFO")
                    .build();

            enrichAssociationFromMetadata(auditLog);
            enrichWithHttpInfo(auditLog);

            auditRepository.save(auditLog);

            log.debug("Audit log créé: {} par {} sur {} #{}",
                    action, user != null ? user.getUsername() : "SYSTEM", entityType, entityId);

        } catch (Exception e) {
            log.error("Erreur création audit log: {}", e.getMessage());
        }
    }

    /**
     * Crée un log d'audit simple (sans entité)
     * Utilisé pour les actions globales
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, Long userId, Map<String, Object> metadata) {
        log(action, null, null, userId, metadata);
    }

    /**
     * Crée un log d'audit avec sévérité personnalisée
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, Long entityId, Long userId,
            Map<String, Object> metadata, String severity) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(userId)
                    .metadata(metadata)
                    .severity(severity)
                    .build();

            enrichWithUserInfo(auditLog, userId);
            enrichAssociationFromMetadata(auditLog);
            enrichWithHttpInfo(auditLog);

            auditRepository.save(auditLog);

            log.debug("Audit log créé: {} [{}] sur {} #{}", action, severity, entityType, entityId);

        } catch (Exception e) {
            log.error("Erreur création audit log: {}", e.getMessage());
        }
    }

    /**
     * Crée un log critique
     * Utilisé pour les actions sensibles
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCritical(String action, String entityType, Long entityId, Long userId,
            Map<String, Object> metadata) {
        log(action, entityType, entityId, userId, metadata, "CRITICAL");
    }

    /**
     * Crée un log d'erreur
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String action, String entityType, Long entityId, Long userId,
            Map<String, Object> metadata) {
        log(action, entityType, entityId, userId, metadata, "ERROR");
    }

    /**
     * Crée un log warning
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWarning(String action, String entityType, Long entityId, Long userId,
            Map<String, Object> metadata) {
        log(action, entityType, entityId, userId, metadata, "WARNING");
    }

    /**
     * Enrichit le log avec les informations HTTP
     */
    private void enrichWithHttpInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Ignorer si pas dans contexte HTTP (tâche async, test, etc.)
            log.trace("Pas de contexte HTTP pour audit log");
        }
    }

    /**
     * Récupère l'adresse IP du client
     * Prend en compte les proxies et load balancers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Prendre la première IP si plusieurs
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Méthode helper pour logger avec description textuelle
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWithDescription(String action, String entityType, Long entityId, Long userId,
            Map<String, Object> metadata, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .userId(userId)
                    .metadata(metadata)
                    .description(description)
                    .severity("INFO")
                    .build();

            enrichWithUserInfo(auditLog, userId);
            enrichAssociationFromMetadata(auditLog);
            enrichWithHttpInfo(auditLog);

            auditRepository.save(auditLog);

            log.debug("Audit log créé: {} - {}", action, description);

        } catch (Exception e) {
            log.error("Erreur création audit log: {}", e.getMessage());
        }

    }

    private void enrichWithUserInfo(AuditLog auditLog, Long userId) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId).ifPresent(user -> {
            auditLog.setUserName(user.getFullName());
            auditLog.setUserEmail(user.getEmail());
            if (user.getAssociation() != null) {
                auditLog.setAssociationId(user.getAssociation().getId());
            }
        });
    }

    private void enrichAssociationFromMetadata(AuditLog auditLog) {
        if (auditLog.getAssociationId() != null || auditLog.getMetadata() == null) {
            return;
        }
        Object associationId = auditLog.getMetadata().get("associationId");
        if (associationId instanceof Number number) {
            auditLog.setAssociationId(number.longValue());
        } else if (associationId instanceof String text) {
            try {
                auditLog.setAssociationId(Long.parseLong(text));
            } catch (NumberFormatException ignored) {
                // Association ID metadata invalide: on ignore.
            }
        }
    }
}
