package bf.kvill.associa.system.audit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entité pour le traçage (audit trail) de toutes les actions dans le système
 *
 * Enregistre :
 * - Qui a fait quoi
 * - Quand
 * - Sur quelle entité
 * - Avec quelles données (metadata)
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_entity_type", columnList = "entity_type"),
        @Index(name = "idx_audit_logs_entity_id", columnList = "entity_id"),
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_action", columnList = "action"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_logs_association_id", columnList = "association_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    /**
     * Action effectuée
     */
    @Column(nullable = false, length = 100)
    @ToString.Include
    private String action;

    /**
     * Type d'entité concernée
     */
    @Column(name = "entity_type", length = 100)
    @ToString.Include
    private String entityType;

    /**
     * ID de l'entité concernée
     *
     * Exemple : Si entityType = "Mandate", entityId = 123
     */
    @Column(name = "entity_id")
    @ToString.Include
    private Long entityId;

    /**
     * ID de l'utilisateur qui a effectué l'action
     * NULL si action système (cron, import auto, etc.)
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Nom complet de l'utilisateur (denormalisé pour performance)
     * Évite les JOINs lors de la consultation des logs
     */
    @Column(name = "user_name", length = 200)
    private String userName;

    /**
     * Email de l'utilisateur (denormalisé)
     */
    @Column(name = "user_email", length = 100)
    private String userEmail;

    /**
     * ID de l'association concernée (pour filtrage rapide)
     * Permet de voir tous les logs d'une association sans JOIN
     */
    @Column(name = "association_id")
    private Long associationId;

    /**
     * Adresse IP de l'utilisateur
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User Agent (navigateur/app)
     */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    /**
     * Métadonnées additionnelles au format JSONB
     *
     * Contient des informations spécifiques à l'action
     *
     * Exemples :
     *
     * ASSIGN_POST:
     * {
     * "postId": 5,
     * "postName": "Président",
     * "userId": 123,
     * "userName": "Jean Dupont",
     * "startDate": "2024-01-01"
     * }
     *
     * APPROVE_TRANSACTION:
     * {
     * "transactionId": 456,
     * "amount": 50000,
     * "type": "EXPENSE",
     * "approver": "Marie Doe"
     * }
     *
     * DELETE_USER:
     * {
     * "userId": 789,
     * "userName": "User Supprimé",
     * "reason": "Demande RGPD"
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Description textuelle de l'action (optionnel)
     * Pour logs plus lisibles
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Niveau de sévérité (optionnel)
     * INFO, WARNING, ERROR, CRITICAL
     */
    @Column(length = 20)
    private String severity;

    /**
     * Date et heure de l'action
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (severity == null) {
            severity = "INFO";
        }
    }

    // ==================== Manual Getters/Setters (Lombok fallback)
    // ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getAssociationId() {
        return associationId;
    }

    public void setAssociationId(Long associationId) {
        this.associationId = associationId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== Manual Builder (Lombok fallback) ====================

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private String action;
        private String entityType;
        private Long entityId;
        private Long userId;
        private String userName;
        private String userEmail;
        private Long associationId;
        private Map<String, Object> metadata;
        private String severity;
        private String ipAddress;
        private String userAgent;

        AuditLogBuilder() {
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public AuditLogBuilder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public AuditLogBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public AuditLogBuilder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public AuditLogBuilder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public AuditLogBuilder associationId(Long associationId) {
            this.associationId = associationId;
            return this;
        }

        public AuditLogBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public AuditLogBuilder severity(String severity) {
            this.severity = severity;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AuditLogBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditLog build() {
            AuditLog auditLog = new AuditLog();
            auditLog.action = this.action;
            auditLog.entityType = this.entityType;
            auditLog.entityId = this.entityId;
            auditLog.userId = this.userId;
            auditLog.userName = this.userName;
            auditLog.userEmail = this.userEmail;
            auditLog.associationId = this.associationId;
            auditLog.metadata = this.metadata;
            auditLog.severity = this.severity;
            auditLog.ipAddress = this.ipAddress;
            auditLog.userAgent = this.userAgent;
            return auditLog;
        }
    }

    // ==================== Business Methods ====================

    /**
     * Vérifie si le log est critique
     */
    public boolean isCritical() {
        return "CRITICAL".equals(severity);
    }

    /**
     * Vérifie si le log est une erreur
     */
    public boolean isError() {
        return "ERROR".equals(severity);
    }

    /**
     * Vérifie si le log est un warning
     */
    public boolean isWarning() {
        return "WARNING".equals(severity);
    }

    /**
     * Vérifie si l'action a été effectuée par un utilisateur
     */
    public boolean hasUser() {
        return userId != null;
    }

    /**
     * Vérifie si l'action est liée à une entité
     */
    public boolean hasEntity() {
        return entityType != null && entityId != null;
    }

    /**
     * Récupère une métadonnée spécifique
     */
    public Object getMetadata(String key) {
        if (metadata == null)
            return null;
        return metadata.get(key);
    }

    /**
     * Ajoute une métadonnée
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
