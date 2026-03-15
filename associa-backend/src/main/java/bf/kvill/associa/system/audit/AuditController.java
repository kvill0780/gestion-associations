package bf.kvill.associa.system.audit;

import bf.kvill.associa.security.userdetails.CustomUserPrincipal;
import bf.kvill.associa.system.audit.dto.*;
import bf.kvill.associa.shared.dto.PageResponse;
import bf.kvill.associa.system.audit.mapper.AuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour consulter les logs d'audit
 */
@RestController
@RequestMapping("/api/system/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PRESIDENT', 'SECRETARY')")
public class AuditController {

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    /**
     * Liste paginée des logs d'audit
     */
    @GetMapping("/logs")
    public ResponseEntity<PageResponse<AuditLogDto>> getLogs(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long associationId = requireAssociationId(principal);
        Page<AuditLog> page = isSuperAdmin(principal)
                ? auditRepository.findAllByOrderByCreatedAtDesc(pageable)
                : auditRepository.findByAssociationIdOrderByCreatedAtDesc(associationId, pageable);

        return ResponseEntity.ok(toPageResponse(page));
    }

    /**
     * Détails d'un log
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<AuditLogDto> getLog(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AuditLog log = auditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log non trouvé"));
        if (!isSuperAdmin(principal)) {
            ensureAssociationAccess(principal, log.getAssociationId());
        }

        return ResponseEntity.ok(auditMapper.toDto(log));
    }

    /**
     * Recherche avec filtres
     */
    @GetMapping("/logs/search")
    public ResponseEntity<PageResponse<AuditLogDto>> searchLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long associationId,
            @RequestParam(required = false) String severity,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long effectiveAssociationId = associationId;
        if (!isSuperAdmin(principal)) {
            Long principalAssociationId = requireAssociationId(principal);
            if (associationId != null && !Objects.equals(associationId, principalAssociationId)) {
                throw new AccessDeniedException("Accès interdit hors de votre association");
            }
            effectiveAssociationId = principalAssociationId;
        }

        Page<AuditLog> page = auditRepository.searchLogs(
                action, entityType, userId, effectiveAssociationId, severity, pageable
        );

        return ResponseEntity.ok(toPageResponse(page));
    }

    /**
     * Logs d'une entité spécifique
     */
    @GetMapping("/logs/entity")
    public ResponseEntity<List<AuditLogDto>> getEntityLogs(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AuditLog> logs = isSuperAdmin(principal)
                ? auditRepository.findByEntityTypeAndEntityId(entityType, entityId)
                : auditRepository.findByEntityTypeAndEntityIdAndAssociationIdOrderByCreatedAtDesc(
                        entityType,
                        entityId,
                        requireAssociationId(principal));

        List<AuditLogDto> dtos = logs.stream()
                .map(auditMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Logs d'un utilisateur
     */
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<PageResponse<AuditLogDto>> getUserLogs(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long associationId = requireAssociationId(principal);
        Page<AuditLog> page = isSuperAdmin(principal)
                ? auditRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                : auditRepository.findByUserIdAndAssociationIdOrderByCreatedAtDesc(
                        userId,
                        associationId,
                        pageable);

        return ResponseEntity.ok(toPageResponse(page));
    }

    /**
     * Logs critiques récents
     */
    @GetMapping("/logs/critical")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AuditLogDto>> getCriticalLogs(
            @RequestParam(defaultValue = "100") int limit
    ) {
        Pageable pageable = Pageable.ofSize(limit);
        List<AuditLog> logs = auditRepository.findRecentCriticalLogs(pageable);

        List<AuditLogDto> dtos = logs.stream()
                .map(auditMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Statistiques d'audit
     */
    @GetMapping("/stats")
    public ResponseEntity<AuditStatsDto> getStats(
            @RequestParam(required = false) Long associationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        LocalDateTime last7days = LocalDateTime.now().minusDays(7);

        Long effectiveAssociationId = associationId;
        if (!isSuperAdmin(principal)) {
            Long principalAssociationId = requireAssociationId(principal);
            if (associationId != null && !Objects.equals(associationId, principalAssociationId)) {
                throw new AccessDeniedException("Accès interdit hors de votre association");
            }
            effectiveAssociationId = principalAssociationId;
        }

        long totalLogs;
        long logsLast24h;
        long logsLast7days;
        long criticalLogs;
        long errorLogs;
        long warningLogs;

        if (effectiveAssociationId == null) {
            totalLogs = auditRepository.count();
            logsLast24h = auditRepository.countSince(last24h);
            logsLast7days = auditRepository.countSince(last7days);
            criticalLogs = auditRepository.countBySeverity("CRITICAL");
            errorLogs = auditRepository.countBySeverity("ERROR");
            warningLogs = auditRepository.countBySeverity("WARNING");
        } else {
            totalLogs = auditRepository.countByAssociationId(effectiveAssociationId);
            logsLast24h = auditRepository.countByAssociationIdAndCreatedAtGreaterThanEqual(
                    effectiveAssociationId,
                    last24h);
            logsLast7days = auditRepository.countByAssociationIdAndCreatedAtGreaterThanEqual(
                    effectiveAssociationId,
                    last7days);
            criticalLogs = auditRepository.countByAssociationIdAndSeverity(effectiveAssociationId, "CRITICAL");
            errorLogs = auditRepository.countByAssociationIdAndSeverity(effectiveAssociationId, "ERROR");
            warningLogs = auditRepository.countByAssociationIdAndSeverity(effectiveAssociationId, "WARNING");
        }

        AuditStatsDto stats = AuditStatsDto.builder()
                .totalLogs(totalLogs)
                .logsLast24h(logsLast24h)
                .logsLast7days(logsLast7days)
                .criticalLogs(criticalLogs)
                .errorLogs(errorLogs)
                .warningLogs(warningLogs)
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Logs par période
     */
    @GetMapping("/logs/period")
    public ResponseEntity<List<AuditLogDto>> getLogsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long associationId = requireAssociationId(principal);
        List<AuditLog> logs = isSuperAdmin(principal)
                ? auditRepository.findByDateRange(startDate, endDate)
                : auditRepository.findByAssociationIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        associationId,
                        startDate,
                        endDate);

        List<AuditLogDto> dtos = logs.stream()
                .map(auditMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    private void ensureAssociationAccess(CustomUserPrincipal principal, Long associationId) {
        if (isSuperAdmin(principal)) {
            return;
        }
        if (associationId == null || !Objects.equals(principal.getAssociationId(), associationId)) {
            throw new AccessDeniedException("Accès interdit hors de votre association");
        }
    }

    private boolean isSuperAdmin(CustomUserPrincipal principal) {
        if (principal == null || principal.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : principal.getAuthorities()) {
            if (authority != null && "ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long requireAssociationId(CustomUserPrincipal principal) {
        if (isSuperAdmin(principal)) {
            return null;
        }
        Long associationId = principal != null ? principal.getAssociationId() : null;
        if (associationId == null) {
            throw new AccessDeniedException("Association introuvable pour ce compte");
        }
        return associationId;
    }

    private PageResponse<AuditLogDto> toPageResponse(Page<AuditLog> page) {
        List<AuditLogDto> content = page.getContent().stream()
                .map(auditMapper::toDto)
                .collect(Collectors.toList());
        return PageResponse.<AuditLogDto>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
