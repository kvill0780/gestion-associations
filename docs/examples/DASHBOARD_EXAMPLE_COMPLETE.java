// ============================================================================
// EXEMPLE COMPLET - DashboardService.java MINIMAL
// Version simplifiée pour démarrage rapide (2-3h)
// Copier ce fichier dans: src/main/java/bf/kvill/associa/dashboard/
// ============================================================================

package bf.kvill.associa.dashboard;

import bf.kvill.associa.dashboard.dto.*;
import bf.kvill.associa.finance.transaction.TransactionRepository;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AssociationRepository associationRepository;
    private final AuditRepository auditRepository;

    public DashboardDto buildDashboard(Long userId, Long associationId) {
        log.info("Building dashboard for user {} in association {}", userId, associationId);
        validateAccess(userId, associationId);

        return DashboardDto.builder()
            .members(buildMembersStats(associationId))
            .finances(buildFinancesStats(associationId))
            .recentActivities(buildRecentActivities(associationId, 10))
            .build();
    }

    private void validateAccess(Long userId, Long associationId) {
        Association association = associationRepository.findById(associationId)
            .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));

        boolean belongs = userRepository.findById(userId)
            .map(user -> user.getAssociation() != null 
                && user.getAssociation().getId().equals(associationId))
            .orElse(false);

        if (!belongs) {
            throw new IllegalStateException("User does not belong to this association");
        }
    }

    private MembersStatsDto buildMembersStats(Long associationId) {
        Long total = userRepository.countByAssociationId(associationId);
        Long active = userRepository.countByAssociationIdAndMembershipStatus(
            associationId, MembershipStatus.ACTIVE
        );
        Long pending = userRepository.countByAssociationIdAndMembershipStatus(
            associationId, MembershipStatus.PENDING
        );
        Long suspended = userRepository.countByAssociationIdAndMembershipStatus(
            associationId, MembershipStatus.SUSPENDED
        );

        return MembersStatsDto.builder()
            .total(total)
            .active(active)
            .pending(pending)
            .suspended(suspended)
            .build();
    }

    private FinancesStatsDto buildFinancesStats(Long associationId) {
        BigDecimal balance = transactionRepository.calculateBalance(associationId);
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        BigDecimal monthlyIncome = transactionRepository.sumByTypeAndDateAfter(
            associationId, TransactionType.INCOME, monthStart
        );

        BigDecimal monthlyExpenses = transactionRepository.sumByTypeAndDateAfter(
            associationId, TransactionType.EXPENSE, monthStart
        );

        List<MonthlyFinanceDto> monthlyData = buildMonthlyFinanceData(associationId, 6);

        return FinancesStatsDto.builder()
            .currentBalance(balance)
            .monthlyIncome(monthlyIncome)
            .monthlyExpenses(monthlyExpenses)
            .netMonthly(monthlyIncome.subtract(monthlyExpenses))
            .monthlyData(monthlyData)
            .build();
    }

    private List<MonthlyFinanceDto> buildMonthlyFinanceData(Long associationId, int monthsCount) {
        List<MonthlyFinanceDto> monthlyData = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        for (int i = monthsCount - 1; i >= 0; i--) {
            LocalDate month = currentMonth.minusMonths(i);
            LocalDate nextMonth = month.plusMonths(1);

            BigDecimal income = transactionRepository.sumByTypeAndDateBetween(
                associationId, TransactionType.INCOME, month, nextMonth
            );

            BigDecimal expenses = transactionRepository.sumByTypeAndDateBetween(
                associationId, TransactionType.EXPENSE, month, nextMonth
            );

            monthlyData.add(MonthlyFinanceDto.builder()
                .month(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH))
                .income(income)
                .expenses(expenses)
                .balance(income.subtract(expenses))
                .build());
        }

        return monthlyData;
    }

    private List<ActivityDto> buildRecentActivities(Long associationId, int limit) {
        return auditRepository
            .findTopNByAssociationOrderByCreatedAtDesc(associationId, limit)
            .stream()
            .map(log -> ActivityDto.builder()
                .description(buildActivityDescription(log))
                .entityType(log.getTargetType())
                .action(log.getAction())
                .createdAt(log.getCreatedAt())
                .userName(log.getPerformedBy() != null 
                    ? log.getPerformedBy().getFullName() 
                    : "Système")
                .build())
            .toList();
    }

    private String buildActivityDescription(bf.kvill.associa.system.audit.AuditLog log) {
        String userName = log.getPerformedBy() != null 
            ? log.getPerformedBy().getFullName() 
            : "Système";

        return switch (log.getAction()) {
            case "CREATE_USER" -> userName + " a créé un nouveau membre";
            case "APPROVE_MEMBER" -> userName + " a approuvé un membre";
            case "CREATE_TRANSACTION" -> userName + " a créé une transaction";
            case "APPROVE_TRANSACTION" -> userName + " a approuvé une transaction";
            case "ASSIGN_POST" -> userName + " a attribué un poste";
            default -> userName + " a effectué une action";
        };
    }
}
