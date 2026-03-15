package bf.kvill.associa.finance.contribution;

import bf.kvill.associa.finance.contribution.dto.CreateContributionRequest;
import bf.kvill.associa.finance.contribution.dto.GenerateContributionsRequest;
import bf.kvill.associa.finance.contribution.dto.RecordContributionPaymentRequest;
import bf.kvill.associa.finance.transaction.Transaction;
import bf.kvill.associa.finance.transaction.TransactionRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.ContributionType;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import bf.kvill.associa.shared.exception.ResourceNotFoundException;
import bf.kvill.associa.system.association.Association;
import bf.kvill.associa.system.association.AssociationRepository;
import bf.kvill.associa.system.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final AssociationRepository associationRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Contribution> findByPeriod(Long associationId, Integer year, Integer month) {
        if (associationId == null) {
            throw new AccessDeniedException("Association introuvable pour ce compte");
        }
        if (year == null) {
            return contributionRepository.findByAssociationIdWithPayments(associationId);
        }
        if (month == null) {
            return contributionRepository.findByAssociationIdAndYearWithPayments(associationId, year);
        }
        return contributionRepository.findByAssociationIdAndYearAndMonthWithPayments(associationId, year, month);
    }

    @Transactional(readOnly = true)
    public Contribution findByIdAndAssociation(Long contributionId, Long associationId) {
        return contributionRepository.findByIdAndAssociationId(contributionId, associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Contribution", contributionId));
    }

    @Transactional
    public Contribution createContribution(Long associationId, Long createdById, CreateContributionRequest request) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));
        User member = userRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getMemberId()));

        assertMemberInAssociation(member, associationId);

        Integer year = request.getYear();
        Integer month = request.getMonth();
        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Le mois doit etre entre 1 et 12");
        }

        if (month != null && contributionRepository.existsByAssociationIdAndMemberIdAndYearAndMonth(
                associationId, member.getId(), year, month)) {
            throw new IllegalStateException("Cotisation deja existante pour cette periode");
        }
        if (month == null && contributionRepository.existsByAssociationIdAndMemberIdAndYearAndMonthIsNull(
                associationId, member.getId(), year)) {
            throw new IllegalStateException("Cotisation annuelle deja existante pour cette annee");
        }

        ContributionType type = resolveType(request.getType(), month);
        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : defaultDueDate(year, month);

        Contribution contribution = Contribution.builder()
                .association(association)
                .member(member)
                .year(year)
                .month(month)
                .type(type)
                .expectedAmount(request.getExpectedAmount())
                .dueDate(dueDate)
                .waived(Boolean.TRUE.equals(request.getWaived()))
                .waivedReason(request.getWaivedReason())
                .notes(request.getNotes())
                .build();

        Contribution saved = contributionRepository.save(contribution);

        auditService.log("CREATE_CONTRIBUTION", "Contribution", saved.getId(), createdById,
                Map.of("memberId", member.getId(), "period", saved.getPeriodLabel()));

        return saved;
    }

    @Transactional
    public List<Contribution> generateContributions(Long associationId, Long createdById,
            GenerateContributionsRequest request) {
        Association association = associationRepository.findById(associationId)
                .orElseThrow(() -> new ResourceNotFoundException("Association", associationId));

        Integer year = request.getYear();
        Integer month = request.getMonth();

        if (month != null && (month < 1 || month > 12)) {
            throw new IllegalArgumentException("Le mois doit etre entre 1 et 12");
        }

        ContributionType type = resolveType(request.getType(), month);
        BigDecimal expectedAmount = request.getExpectedAmount() != null
                ? request.getExpectedAmount()
                : association.getDefaultMembershipFee();

        LocalDate dueDate = request.getDueDate() != null ? request.getDueDate() : defaultDueDate(year, month);

        List<User> activeMembers = userRepository
                .findByAssociationIdAndMembershipStatus(associationId, MembershipStatus.ACTIVE);

        List<Contribution> toCreate = new ArrayList<>();
        for (User member : activeMembers) {
            boolean exists = month != null
                    ? contributionRepository.existsByAssociationIdAndMemberIdAndYearAndMonth(
                            associationId, member.getId(), year, month)
                    : contributionRepository.existsByAssociationIdAndMemberIdAndYearAndMonthIsNull(
                            associationId, member.getId(), year);

            if (exists) {
                continue;
            }

            Contribution contribution = Contribution.builder()
                    .association(association)
                    .member(member)
                    .year(year)
                    .month(month)
                    .type(type)
                    .expectedAmount(expectedAmount)
                    .dueDate(dueDate)
                    .build();
            toCreate.add(contribution);
        }

        List<Contribution> saved = contributionRepository.saveAll(toCreate);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("year", year);
        metadata.put("month", month != null ? month : "all");
        metadata.put("count", saved.size());
        auditService.log("GENERATE_CONTRIBUTIONS", "Contribution", null, createdById, metadata);

        return saved;
    }

    @Transactional
    public Contribution recordPayment(Long associationId, Long contributionId, Long recordedById,
            RecordContributionPaymentRequest request) {
        Contribution contribution = findByIdAndAssociation(contributionId, associationId);
        User member = contribution.getMember();
        assertMemberInAssociation(member, associationId);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit etre strictement positif");
        }

        Transaction transaction = new Transaction();
        transaction.setAssociation(contribution.getAssociation());
        transaction.setUser(member);
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(TransactionCategory.MEMBERSHIP_FEE);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(request.getTransactionDate() != null
                ? request.getTransactionDate()
                : LocalDate.now());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setNotes(request.getNotes());
        transaction.setRecordedById(recordedById);
        transaction.setTitle("Cotisation " + contribution.getPeriodLabel());
        transaction.setDescription("Paiement cotisation - " + member.getFullName());

        boolean approvalWorkflow = Boolean.TRUE.equals(contribution.getAssociation().getFinanceApprovalWorkflow());
        if (!approvalWorkflow) {
            transaction.setStatus(TransactionStatus.APPROVED);
            transaction.setValidatedById(recordedById);
            transaction.setValidatedAt(LocalDateTime.now());
        } else {
            transaction.setStatus(TransactionStatus.PENDING);
        }

        contribution.addPayment(transaction);
        transactionRepository.save(transaction);
        Contribution saved = contributionRepository.save(contribution);

        if (transaction.getStatus() == TransactionStatus.APPROVED && contribution.isPaid()) {
            if (member.getMembershipStatus() != MembershipStatus.ACTIVE) {
                member.activateMembership();
                userRepository.save(member);
            }
        }

        auditService.log("CONTRIBUTION_PAYMENT", "Contribution", saved.getId(), recordedById,
                Map.of("amount", transaction.getAmount(), "status", transaction.getStatus().name()));

        return saved;
    }

    private void assertMemberInAssociation(User member, Long associationId) {
        Long memberAssociationId = member.getAssociation() != null ? member.getAssociation().getId() : null;
        if (memberAssociationId == null || !memberAssociationId.equals(associationId)) {
            throw new AccessDeniedException("Action interdite hors de votre association");
        }
    }

    private ContributionType resolveType(ContributionType type, Integer month) {
        if (type != null) {
            return type;
        }
        return month != null ? ContributionType.MONTHLY : ContributionType.ANNUAL;
    }

    private LocalDate defaultDueDate(Integer year, Integer month) {
        if (year == null) {
            return LocalDate.now();
        }
        if (month == null) {
            return LocalDate.of(year, 12, 31);
        }
        return YearMonth.of(year, month).atEndOfMonth();
    }
}
