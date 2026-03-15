package bf.kvill.associa.system.association;

import bf.kvill.associa.shared.enums.AssociationStatus;
import bf.kvill.associa.shared.enums.AssociationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des associations
 */
@Repository
public interface AssociationRepository extends JpaRepository<Association, Long> {

    // ==================== RECHERCHE PAR SLUG ====================

    Optional<Association> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // ==================== RECHERCHE PAR STATUT ====================

    List<Association> findByStatus(AssociationStatus status);

    Page<Association> findByStatus(AssociationStatus status, Pageable pageable);

    List<Association> findByStatusOrderByNameAsc(AssociationStatus status);

    List<Association> findByStatusIn(List<AssociationStatus> statuses);

    // ==================== RECHERCHE PAR TYPE ====================

    List<Association> findByType(AssociationType type);

    List<Association> findByTypeOrderByNameAsc(AssociationType type);

    // ==================== RECHERCHE COMBINÉE ====================

    List<Association> findByTypeAndStatus(AssociationType type, AssociationStatus status);

    @Query("SELECT a FROM Association a WHERE a.type = :type AND a.status IN :statuses")
    List<Association> findByTypeAndStatusIn(
            @Param("type") AssociationType type,
            @Param("statuses") List<AssociationStatus> statuses
    );

    // ==================== RECHERCHE PAR NOM ====================

    List<Association> findByNameContainingIgnoreCase(String name);

    @Query("SELECT a FROM Association a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.slug) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Association> searchByNameOrSlug(@Param("query") String query);

    // ==================== RECHERCHE AVEC FILTRES ====================

    /**
     * Recherche avancée avec filtres multiples
     */
    @Query("SELECT a FROM Association a WHERE " +
            "(:type IS NULL OR a.type = :type) AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:query IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Association> searchWithFilters(
            @Param("type") AssociationType type,
            @Param("status") AssociationStatus status,
            @Param("query") String query
    );

    // ==================== STATISTIQUES ====================

    long countByStatus(AssociationStatus status);

    long countByType(AssociationType type);

    long countByTypeAndStatus(AssociationType type, AssociationStatus status);

    @Query("SELECT COUNT(a) FROM Association a WHERE a.createdAt >= :startDate")
    long countCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);

    // ==================== ASSOCIATIONS ACTIVES ====================

    @Query("SELECT a FROM Association a WHERE a.status = 'ACTIVE' ORDER BY a.name ASC")
    List<Association> findAllActive();

    Page<Association> findAllByStatusOrderByNameAsc(AssociationStatus status, Pageable pageable);

    // ==================== ASSOCIATIONS AVEC MEMBRES ====================

    /**
     * Récupère les associations ayant au moins N membres actifs
     */
    @Query("SELECT DISTINCT a FROM Association a " +
            "JOIN a.members m " +
            "WHERE m.membershipStatus = 'ACTIVE' " +
            "GROUP BY a " +
            "HAVING COUNT(m) >= :minMembers")
    List<Association> findWithAtLeastMembers(@Param("minMembers") long minMembers);

    // ==================== ASSOCIATIONS RÉCENTES ====================

    @Query("SELECT a FROM Association a ORDER BY a.createdAt DESC")
    List<Association> findRecentAssociations(Pageable pageable);

    // ==================== ASSOCIATIONS PAR CRÉATEUR ====================

    List<Association> findByCreatedById(Long userId);

    @Query("SELECT a FROM Association a WHERE a.createdBy.id = :userId AND a.status = :status")
    List<Association> findByCreatedByIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") AssociationStatus status
    );
}