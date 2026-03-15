package bf.kvill.associa.finance.transaction;

import bf.kvill.associa.shared.enums.TransactionCategory;
import bf.kvill.associa.shared.enums.TransactionStatus;
import bf.kvill.associa.shared.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Transaction
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        /**
         * Recherche les transactions d'une association
         */
        List<Transaction> findByAssociationId(Long associationId);

        /**
         * Recherche les transactions par statut
         */
        List<Transaction> findByStatus(TransactionStatus status);

        /**
         * Recherche les transactions d'une association par statut
         */
        List<Transaction> findByAssociationIdAndStatus(Long associationId, TransactionStatus status);

        /**
         * Recherche les transactions par type
         */
        List<Transaction> findByType(TransactionType type);

        /**
         * Recherche les transactions d'une association par type
         */
        List<Transaction> findByAssociationIdAndType(Long associationId, TransactionType type);

        /**
         * Recherche les transactions par catégorie
         */
        List<Transaction> findByCategory(TransactionCategory category);

        /**
         * Recherche les transactions d'un utilisateur
         */
        List<Transaction> findByUserId(Long userId);

        /**
         * Recherche les transactions enregistrées par un utilisateur
         */
        List<Transaction> findByRecordedById(Long recordedById);

        /**
         * Recherche les transactions validées par un utilisateur
         */
        List<Transaction> findByValidatedById(Long validatedById);

        /**
         * Recherche les transactions par période
         */
        @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
        List<Transaction> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        /**
         * Recherche les transactions d'une association par période
         */
        @Query("SELECT t FROM Transaction t WHERE t.association.id = :associationId AND t.transactionDate BETWEEN :startDate AND :endDate")
        List<Transaction> findByAssociationAndDateRange(@Param("associationId") Long associationId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        /**
         * Calcule le solde d'une association (recettes - dépenses approuvées)
         */
        @Query("SELECT COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
                        "FROM Transaction t WHERE t.association.id = :associationId AND t.status = 'APPROVED'")
        BigDecimal calculateBalance(@Param("associationId") Long associationId);

        /**
         * Recherche les cotisations d'un utilisateur
         */
        @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.category = 'MEMBERSHIP_FEE' ORDER BY t.transactionDate DESC")
        List<Transaction> findMembershipPaymentsByUser(@Param("userId") Long userId);

        /**
         * Recherche les transactions en attente d'approbation
         */
        List<Transaction> findByAssociationIdOrderByCreatedAtDesc(Long associationId);

        List<Transaction> findByAssociationIdAndStatusOrderByCreatedAtDesc(Long associationId,
                        TransactionStatus status);

        List<Transaction> findByAssociationIdAndTypeAndStatus(Long associationId, TransactionType type,
                        TransactionStatus status);

        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.association.id = :associationId AND t.type = :type AND t.status = 'APPROVED' AND t.transactionDate >= :startDate")
        BigDecimal sumApprovedAmountByTypeAndDateAfter(@Param("associationId") Long associationId,
                        @Param("type") TransactionType type,
                        @Param("startDate") LocalDate startDate);

        long countByAssociationIdAndStatus(Long associationId, TransactionStatus status);

        @Query("SELECT t FROM Transaction t WHERE t.id = :transactionId AND t.association.id = :associationId")
        Optional<Transaction> findByIdAndAssociationId(
                        @Param("transactionId") Long transactionId,
                        @Param("associationId") Long associationId);
}
