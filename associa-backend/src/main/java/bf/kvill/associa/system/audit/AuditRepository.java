package bf.kvill.associa.system.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour les logs d'audit
 */
@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {

    // ==================== RECHERCHE PAR ACTION ====================

    List<AuditLog> findByAction(String action);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    List<AuditLog> findByActionIn(List<String> actions);

    // ==================== RECHERCHE PAR ENTITÉ ====================

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    List<AuditLog> findByEntityTypeAndEntityIdAndAssociationIdOrderByCreatedAtDesc(
            String entityType,
            Long entityId,
            Long associationId);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    List<AuditLog> findByEntityType(String entityType);

    // ==================== RECHERCHE PAR UTILISATEUR ====================

    List<AuditLog> findByUserId(Long userId);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<AuditLog> findByUserIdAndAssociationIdOrderByCreatedAtDesc(Long userId, Long associationId, Pageable pageable);

    // ==================== RECHERCHE PAR ASSOCIATION ====================

    List<AuditLog> findByAssociationId(Long associationId);

    Page<AuditLog> findByAssociationId(Long associationId, Pageable pageable);

    Page<AuditLog> findByAssociationIdOrderByCreatedAtDesc(Long associationId, Pageable pageable);

    // ==================== RECHERCHE PAR PÉRIODE ====================

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<AuditLog> findByAssociationIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long associationId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // ==================== RECHERCHE PAR SÉVÉRITÉ ====================

    List<AuditLog> findBySeverity(String severity);

    Page<AuditLog> findBySeverityOrderByCreatedAtDesc(String severity, Pageable pageable);

    List<AuditLog> findBySeverityIn(List<String> severities);

    // ==================== RECHERCHE COMBINÉE ====================

    @Query("SELECT al FROM AuditLog al WHERE " +
            "(:action IS NULL OR al.action = :action) AND " +
            "(:entityType IS NULL OR al.entityType = :entityType) AND " +
            "(:userId IS NULL OR al.userId = :userId) AND " +
            "(:associationId IS NULL OR al.associationId = :associationId) AND " +
            "(:severity IS NULL OR al.severity = :severity) " +
            "ORDER BY al.createdAt DESC")
    Page<AuditLog> searchLogs(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("userId") Long userId,
            @Param("associationId") Long associationId,
            @Param("severity") String severity,
            Pageable pageable
    );

    // ==================== STATISTIQUES ====================

    long countByAction(String action);

    long countByUserId(Long userId);

    long countByAssociationId(Long associationId);

    long countByAssociationIdAndCreatedAtGreaterThanEqual(Long associationId, LocalDateTime since);

    long countByAssociationIdAndSeverity(Long associationId, String severity);

    long countBySeverity(String severity);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);

    // ==================== LOGS RÉCENTS ====================

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.severity IN ('CRITICAL', 'ERROR') ORDER BY al.createdAt DESC")
    List<AuditLog> findRecentCriticalLogs(Pageable pageable);

    // ==================== LOGS PAR IP ====================

    List<AuditLog> findByIpAddress(String ipAddress);

    @Query("SELECT al FROM AuditLog al WHERE al.ipAddress = :ipAddress AND al.createdAt >= :since")
    List<AuditLog> findByIpAddressSince(
            @Param("ipAddress") String ipAddress,
            @Param("since") LocalDateTime since
    );

    // ==================== NETTOYAGE ====================

    @Query("DELETE FROM AuditLog al WHERE al.createdAt < :before AND al.severity = 'INFO'")
    void deleteOldInfoLogs(@Param("before") LocalDateTime before);
}
