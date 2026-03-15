package bf.kvill.associa.dashboard;

import bf.kvill.associa.dashboard.dto.*;
import bf.kvill.associa.documents.DocumentRepository;
import bf.kvill.associa.documents.Document;
import bf.kvill.associa.finance.transaction.TransactionRepository;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import bf.kvill.associa.system.audit.AuditLog;
import bf.kvill.associa.system.audit.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

        private final UserRepository userRepository;
        private final TransactionRepository transactionRepository;
        private final AuditRepository auditRepository;
        private final DocumentRepository documentRepository;
        // private final EventRepository eventRepository; // TODO: Implement module

        public DashboardDto buildDashboard(Long userId, Long associationId) {
                log.info("Building dashboard for user {} in association {}", userId, associationId);

                return DashboardDto.builder()
                                .members(buildMembersStats(associationId))
                                .finances(buildFinancesStats(associationId))
                                .events(buildEventsStats(associationId))
                                .documents(buildDocumentsStats(associationId))
                                .recentActivities(buildRecentActivities(associationId))
                                .build();
        }

        private MembersStatsDto buildMembersStats(Long associationId) {
                long total = userRepository.countByAssociationId(associationId);
                long active = userRepository.countByAssociationIdAndMembershipStatus(
                                associationId, MembershipStatus.ACTIVE);
                long pending = userRepository.countByAssociationIdAndMembershipStatus(
                                associationId, MembershipStatus.PENDING);

                return MembersStatsDto.builder()
                                .total(total)
                                .active(active)
                                .pending(pending)
                                .inactive(total - active - pending)
                                .build();
        }

        private FinancesStatsDto buildFinancesStats(Long associationId) {
                BigDecimal balance = transactionRepository.calculateBalance(associationId);
                if (balance == null)
                        balance = BigDecimal.ZERO;

                LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

                BigDecimal monthlyIncome = transactionRepository.sumApprovedAmountByTypeAndDateAfter(
                                associationId, TransactionType.INCOME, monthStart);
                if (monthlyIncome == null)
                        monthlyIncome = BigDecimal.ZERO;

                BigDecimal monthlyExpenses = transactionRepository.sumApprovedAmountByTypeAndDateAfter(
                                associationId, TransactionType.EXPENSE, monthStart);
                if (monthlyExpenses == null)
                        monthlyExpenses = BigDecimal.ZERO;

                return FinancesStatsDto.builder()
                                .currentBalance(balance)
                                .monthlyIncome(monthlyIncome)
                                .monthlyExpenses(monthlyExpenses)
                                .netMonthly(monthlyIncome.subtract(monthlyExpenses))
                                .budget(BigDecimal.ZERO) // TODO: Implement budget logic
                                .build();
        }

        private EventsStatsDto buildEventsStats(Long associationId) {
                // TODO: Implement real logic when Event module exists
                return EventsStatsDto.builder()
                                .totalUpcoming(0L)
                                .totalPast(0L)
                                .upcoming(Collections.emptyList())
                                .build();
        }

        private DocumentsStatsDto buildDocumentsStats(Long associationId) {
                long total = documentRepository.countByAssociationId(associationId);
                List<DocumentSummaryDto> recent = documentRepository.findTop5ByAssociationIdOrderByCreatedAtDesc(associationId)
                                .stream()
                                .map(this::toDocumentSummary)
                                .toList();

                return DocumentsStatsDto.builder()
                                .total(total)
                                .recent(recent)
                                .build();
        }

        private DocumentSummaryDto toDocumentSummary(Document document) {
                return DocumentSummaryDto.builder()
                                .id(document.getId())
                                .title(document.getTitle())
                                .category(document.getCategory())
                                .fileType(document.getFileType())
                                .fileSize(document.getFileSize())
                                .createdAt(document.getCreatedAt())
                                .build();
        }

        private List<ActivityDto> buildRecentActivities(Long associationId) {
                Pageable top5 = PageRequest.of(0, 5);
                List<AuditLog> recentLogs = auditRepository.findByAssociationIdOrderByCreatedAtDesc(associationId, top5)
                                .getContent();

                return recentLogs.stream()
                                .map(this::mapToActivityDto)
                                .collect(Collectors.toList());
        }

        private ActivityDto mapToActivityDto(AuditLog log) {
                return ActivityDto.builder()
                                // Si pas de description lisible, on fallback sur le code d'action brut (ex:
                                // CREATE_USER)
                                .description(log.getDescription() != null ? log.getDescription() : log.getAction())
                                .entityType(log.getEntityType())
                                .action(log.getAction())
                                .createdAt(log.getCreatedAt())
                                .userName(log.getUserName() != null ? log.getUserName() : "Système")
                                .entityId(log.getEntityId() != null ? String.valueOf(log.getEntityId()) : null)
                                .build();
        }
}
