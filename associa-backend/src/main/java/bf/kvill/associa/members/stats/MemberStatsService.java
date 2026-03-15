package bf.kvill.associa.members.stats;

import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.members.mandate.MandateRepository;
import bf.kvill.associa.shared.enums.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour les statistiques des membres
 * Reproduit MemberStatsService de Laravel
 */
@Service
@RequiredArgsConstructor
public class MemberStatsService {

    private static final Logger log = LoggerFactory.getLogger(MemberStatsService.class);

    private final UserRepository userRepository;
    private final MandateRepository mandateRepository;

    /**
     * Récupère les statistiques globales des membres d'une association
     * 
     * @param associationId ID de l'association
     * @return Map avec les statistiques
     */
    public Map<String, Object> getGlobalStats(Long associationId) {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de membres
        long totalMembers = userRepository.countByAssociationId(associationId);
        stats.put("total_members", totalMembers);

        // Membres actifs
        long activeMembers = userRepository.countByAssociationIdAndMembershipStatus(
                associationId, MembershipStatus.ACTIVE);
        stats.put("active_members", activeMembers);

        // Membres en attente
        long pendingMembers = userRepository.countByAssociationIdAndMembershipStatus(
                associationId, MembershipStatus.PENDING);
        stats.put("pending_members", pendingMembers);

        // Membres suspendus
        long suspendedMembers = userRepository.countByAssociationIdAndMembershipStatus(
                associationId, MembershipStatus.SUSPENDED);
        stats.put("suspended_members", suspendedMembers);

        // Membres inactifs
        long inactiveMembers = userRepository.countByAssociationIdAndMembershipStatus(
                associationId, MembershipStatus.INACTIVE);
        stats.put("inactive_members", inactiveMembers);

        // Mandats actifs
        long activeMandates = mandateRepository.countByAssociationIdAndActiveTrue(associationId);
        stats.put("active_mandates", activeMandates);

        // Taux d'activation (actifs / total)
        double activationRate = totalMembers > 0
                ? (double) activeMembers / totalMembers * 100
                : 0.0;
        stats.put("activation_rate", Math.round(activationRate * 100.0) / 100.0);

        log.debug("Global stats for association {}: {} members, {} active",
                associationId, totalMembers, activeMembers);

        return stats;
    }

    /**
     * Récupère les statistiques par statut
     * 
     * @param associationId ID de l'association
     * @return Map avec le nombre de membres par statut
     */
    public Map<String, Long> getStatsByStatus(Long associationId) {
        Map<String, Long> stats = new HashMap<>();

        for (MembershipStatus status : MembershipStatus.values()) {
            long count = userRepository.countByAssociationIdAndMembershipStatus(
                    associationId, status);
            stats.put(status.name().toLowerCase(), count);
        }

        return stats;
    }

    /**
     * Récupère les statistiques d'adhésion par mois
     * 
     * @param associationId ID de l'association
     * @param year          Année
     * @return Map avec le nombre d'adhésions par mois
     */
    public Map<Integer, Long> getMembershipsByMonth(Long associationId, int year) {
        Map<Integer, Long> stats = new HashMap<>();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31).plusDays(1);

        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            
            long count = userRepository.countByAssociationIdAndMembershipDateBetween(
                associationId, monthStart, monthEnd);
            stats.put(month, count);
        }

        return stats;
    }

    /**
     * Récupère le taux de rétention des membres
     * (Membres actifs depuis plus d'un an / Total membres)
     * 
     * @param associationId ID de l'association
     * @return Taux de rétention en pourcentage
     */
    public double getRetentionRate(Long associationId) {
        long totalActive = userRepository.countByAssociationIdAndMembershipStatus(
                associationId, MembershipStatus.ACTIVE);

        if (totalActive == 0) {
            return 0.0;
        }

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        long longTermMembers = userRepository.countByAssociationIdAndMembershipStatusAndMembershipDateBefore(
                associationId, MembershipStatus.ACTIVE, oneYearAgo);

        double retentionRate = (double) longTermMembers / totalActive * 100;
        return Math.round(retentionRate * 100.0) / 100.0;
    }

    /**
     * Récupère les nouveaux membres du mois
     * 
     * @param associationId ID de l'association
     * @return Nombre de nouveaux membres ce mois
     */
    public long getNewMembersThisMonth(Long associationId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        return userRepository.countByAssociationIdAndMembershipDateGreaterThanEqual(
                associationId, startOfMonth);
    }

    /**
     * Génère un rapport complet des membres
     * 
     * @param associationId ID de l'association
     * @return Map avec toutes les statistiques
     */
    public Map<String, Object> generateFullReport(Long associationId) {
        Map<String, Object> report = new HashMap<>();

        report.put("global_stats", getGlobalStats(associationId));
        report.put("stats_by_status", getStatsByStatus(associationId));
        report.put("retention_rate", getRetentionRate(associationId));
        report.put("new_members_this_month", getNewMembersThisMonth(associationId));
        report.put("generated_at", LocalDate.now());

        log.info("Generated full member report for association {}", associationId);

        return report;
    }
}
