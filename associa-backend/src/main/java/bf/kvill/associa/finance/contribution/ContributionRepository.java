package bf.kvill.associa.finance.contribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    @Query("SELECT DISTINCT c FROM Contribution c LEFT JOIN FETCH c.payments p WHERE c.association.id = :associationId ORDER BY c.dueDate ASC")
    List<Contribution> findByAssociationIdWithPayments(@Param("associationId") Long associationId);

    @Query("SELECT DISTINCT c FROM Contribution c LEFT JOIN FETCH c.payments p WHERE c.association.id = :associationId AND c.year = :year ORDER BY c.dueDate ASC")
    List<Contribution> findByAssociationIdAndYearWithPayments(
            @Param("associationId") Long associationId,
            @Param("year") Integer year);

    @Query("SELECT DISTINCT c FROM Contribution c LEFT JOIN FETCH c.payments p WHERE c.association.id = :associationId AND c.year = :year AND c.month = :month ORDER BY c.dueDate ASC")
    List<Contribution> findByAssociationIdAndYearAndMonthWithPayments(
            @Param("associationId") Long associationId,
            @Param("year") Integer year,
            @Param("month") Integer month);

    Optional<Contribution> findByIdAndAssociationId(Long id, Long associationId);

    @Query("SELECT DISTINCT c FROM Contribution c LEFT JOIN FETCH c.payments p WHERE c.id = :id")
    Optional<Contribution> findByIdWithPayments(@Param("id") Long id);

    boolean existsByAssociationIdAndMemberIdAndYearAndMonth(Long associationId, Long memberId, Integer year, Integer month);

    boolean existsByAssociationIdAndMemberIdAndYearAndMonthIsNull(Long associationId, Long memberId, Integer year);
}
