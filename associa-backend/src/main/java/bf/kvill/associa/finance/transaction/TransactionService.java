package bf.kvill.associa.finance.transaction;

import bf.kvill.associa.finance.contribution.Contribution;
import bf.kvill.associa.finance.contribution.ContributionRepository;
import bf.kvill.associa.members.user.User;
import bf.kvill.associa.members.user.UserRepository;
import bf.kvill.associa.shared.enums.MembershipStatus;
import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import bf.kvill.associa.system.audit.AuditService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion des transactions financières
 * Reproduit TransactionService de Laravel
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final AuditService auditLogService;

    /**
     * Recherche une transaction par ID
     */
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    public Optional<Transaction> findByIdAndAssociationId(Long transactionId, Long associationId) {
        return transactionRepository.findByIdAndAssociationId(transactionId, associationId);
    }

    /**
     * Recherche toutes les transactions d'une association
     */
    public List<Transaction> findByAssociation(Long associationId) {
        return transactionRepository.findByAssociationIdOrderByCreatedAtDesc(associationId);
    }

    /**
     * Recherche les transactions par statut
     */
    public List<Transaction> findByStatus(Long associationId, TransactionStatus status) {
        return transactionRepository.findByAssociationIdAndStatusOrderByCreatedAtDesc(associationId, status);
    }

    /**
     * Crée une nouvelle transaction
     */
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit etre positif");
        }

        if (transaction.getAssociation() == null) {
            throw new IllegalArgumentException("L'association est obligatoire");
        }

        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Le type est obligatoire");
        }

        if (transaction.getRecordedById() == null) {
            throw new IllegalArgumentException("L'enregistreur est obligatoire");
        }

        if (transaction.getStatus() == null) {
            transaction.setStatus(TransactionStatus.PENDING);
        }

        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDate.now());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Created transaction: {} ({}) - {} {}",
                savedTransaction.getId(),
                savedTransaction.getType(),
                savedTransaction.getAmount(),
                savedTransaction.getCategory());

        return savedTransaction;
    }

    /**
     * Approuve une transaction (comme Laravel)
     */
    @Transactional
    public Transaction approveTransaction(Long transactionId, Long validatorId, String notes) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction non trouvee"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Seules les transactions en attente peuvent etre approuvees");
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setValidatedById(validatorId);
        transaction.setValidatedAt(LocalDateTime.now());

        if (notes != null) {
            transaction.setNotes(notes);
        }

        Transaction savedTransaction = transactionRepository.save(transaction);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transaction_id", transactionId);
        metadata.put("amount", transaction.getAmount());
        metadata.put("type", transaction.getType());
        auditLogService.log("approve_transaction", "Transaction", transaction.getId(), getCurrentUser(validatorId),
                metadata);

        handleContributionApproval(savedTransaction);

        log.info("Approved transaction: {} by user {}", transactionId, validatorId);
        return savedTransaction;
    }

    /**
     * Rejette une transaction (comme Laravel)
     */
    @Transactional
    public Transaction rejectTransaction(Long transactionId, Long validatorId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction non trouvee"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Seules les transactions en attente peuvent etre rejetees");
        }

        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setValidatedById(validatorId);
        transaction.setValidatedAt(LocalDateTime.now());
        transaction.setNotes(reason);

        Transaction savedTransaction = transactionRepository.save(transaction);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transaction_id", transactionId);
        metadata.put("reason", reason);
        auditLogService.log("reject_transaction", "Transaction", transaction.getId(), getCurrentUser(validatorId),
                metadata);

        log.info("Rejected transaction: {} by user {} - Reason: {}", transactionId, validatorId, reason);
        return savedTransaction;
    }

    /**
     * Enregistre un paiement de cotisation (comme Laravel)
     */
    @Transactional
    public Transaction recordMembershipPayment(Long userId, BigDecimal amount, Long recordedById) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouve"));

        Transaction transaction = new Transaction();
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(TransactionCategory.MEMBERSHIP_FEE);
        transaction.setAmount(amount);
        transaction.setDescription("Paiement cotisation - " + user.getFirstName() + " " + user.getLastName());
        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setRecordedById(recordedById);
        transaction.setValidatedById(recordedById);
        transaction.setValidatedAt(LocalDateTime.now());
        // TODO: Definir association

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (user.getMembershipStatus() == MembershipStatus.PENDING ||
                user.getMembershipStatus() == MembershipStatus.INACTIVE) {
            user.setMembershipStatus(MembershipStatus.ACTIVE);
            userRepository.save(user);
            log.info("Updated user {} status to ACTIVE after membership payment", userId);
        }

        log.info("Recorded membership payment: {} for user {}", savedTransaction.getId(), userId);
        return savedTransaction;
    }

    /**
     * Calcule le solde actuel d'une association (comme Laravel)
     */
    public BigDecimal calculateBalance(Long associationId) {
        List<Transaction> approvedTransactions = transactionRepository
                .findByAssociationIdAndStatusOrderByCreatedAtDesc(associationId, TransactionStatus.APPROVED);

        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction transaction : approvedTransactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                balance = balance.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                balance = balance.subtract(transaction.getAmount());
            }
        }

        log.debug("Calculated balance for association {}: {}", associationId, balance);
        return balance;
    }

    /**
     * Recupere les statistiques financieres d'une association
     */
    public Map<String, Object> getFinancialStats(Long associationId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("current_balance", calculateBalance(associationId));

        BigDecimal totalIncome = transactionRepository
                .findByAssociationIdAndTypeAndStatus(associationId, TransactionType.INCOME, TransactionStatus.APPROVED)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("total_income", totalIncome);

        BigDecimal totalExpense = transactionRepository
                .findByAssociationIdAndTypeAndStatus(associationId, TransactionType.EXPENSE, TransactionStatus.APPROVED)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("total_expense", totalExpense);

        long pendingCount = transactionRepository
                .countByAssociationIdAndStatus(associationId, TransactionStatus.PENDING);
        stats.put("pending_transactions", pendingCount);

        return stats;
    }

    /**
     * Genere un recu pour une transaction (comme Laravel)
     * TODO: Implementer generation PDF
     */
    public String generateReceipt(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction non trouvee"));

        log.info("Generating receipt for transaction: {}", transactionId);

        return "/receipts/transaction_" + transactionId + ".pdf";
    }

    /**
     * Supprime une transaction (soft delete)
     */
    @Transactional
    public void deleteTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction non trouvee"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Seules les transactions en attente peuvent etre supprimees");
        }

        transactionRepository.delete(transaction);
        log.info("Deleted transaction: {}", transactionId);
    }

    /**
     * Met a jour l'etat du membre apres approbation d'un paiement de cotisation
     */
    private void handleContributionApproval(Transaction transaction) {
        if (transaction == null || transaction.getContribution() == null) {
            return;
        }
        Long contributionId = transaction.getContribution().getId();
        if (contributionId == null) {
            return;
        }
        Contribution contribution = contributionRepository.findByIdWithPayments(contributionId).orElse(null);
        if (contribution == null) {
            return;
        }
        User member = contribution.getMember();
        if (member == null) {
            return;
        }
        if (contribution.isPaid() && member.getMembershipStatus() != MembershipStatus.ACTIVE) {
            member.activateMembership();
            userRepository.save(member);
        }
    }

    /**
     * Recupere l'utilisateur courant (helper)
     */
    private User getCurrentUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
