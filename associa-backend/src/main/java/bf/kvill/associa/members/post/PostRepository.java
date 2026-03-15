package bf.kvill.associa.members.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'accès aux données des postes
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ==================== RECHERCHE PAR ASSOCIATION ====================

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationId(Long associationId);

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    Page<Post> findByAssociationId(Long associationId, Pageable pageable);

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationIdOrderByDisplayOrderAsc(Long associationId);

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationIdAndIsActiveTrueOrderByDisplayOrderAsc(Long associationId);

    // ==================== RECHERCHE PAR TYPE ====================

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByIsExecutiveTrue();

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByIsExecutiveTrueOrderByDisplayOrderAsc();

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationIdAndIsExecutiveTrue(Long associationId);

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationIdAndIsExecutiveTrueOrderByDisplayOrderAsc(Long associationId);

    // ==================== RECHERCHE PAR NOM ====================

    Optional<Post> findByNameAndAssociationId(String name, Long associationId);

    boolean existsByNameAndAssociationId(String name, Long associationId);

    // ==================== RECHERCHE AVEC RÔLES ====================

    /**
     * Récupère un poste avec ses rôles suggérés (eager loading)
     */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.roles LEFT JOIN FETCH p.defaultRole WHERE p.id = :postId")
    Optional<Post> findByIdWithRoles(@Param("postId") Long postId);

    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdWithAssociationAndRoles(@Param("postId") Long postId);

    /**
     * Récupère tous les postes avec rôles pour une association
     */
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.roles LEFT JOIN FETCH p.defaultRole WHERE p.association.id = :associationId ORDER BY p.displayOrder ASC")
    List<Post> findByAssociationIdWithRoles(@Param("associationId") Long associationId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Post p JOIN p.roles r WHERE p.id = :postId AND r.id = :roleId")
    boolean existsRoleLink(@Param("postId") Long postId, @Param("roleId") Long roleId);

    // ==================== STATISTIQUES ====================

    /**
     * Compte le nombre de postes par association
     */
    long countByAssociationId(Long associationId);

    /**
     * Compte le nombre de postes exécutifs par association
     */
    long countByAssociationIdAndIsExecutiveTrue(Long associationId);

    /**
     * Compte le nombre de postes actifs par association
     */
    long countByAssociationIdAndIsActiveTrue(Long associationId);

    // ==================== RECHERCHE AVANCÉE ====================

    /**
     * Trouve les postes vacants (aucun mandat actif)
     */
    @Query("SELECT p FROM Post p WHERE p.association.id = :associationId " +
            "AND p.isActive = true " +
            "AND NOT EXISTS (SELECT m FROM Mandate m WHERE m.post.id = p.id AND m.active = true)")
    List<Post> findVacantPostsByAssociation(@Param("associationId") Long associationId);

    /**
     * Trouve les postes pouvant accepter de nouveaux mandats
     * (actifs et non pleins)
     */
    @Query("SELECT p FROM Post p WHERE p.association.id = :associationId " +
            "AND p.isActive = true " +
            "AND (p.maxOccupants IS NULL " +
            "     OR (SELECT COUNT(m) FROM Mandate m WHERE m.post.id = p.id AND m.active = true) < p.maxOccupants)")
    List<Post> findAvailablePostsByAssociation(@Param("associationId") Long associationId);

    /**
     * Trouve les postes nécessitant une élection
     */
    @EntityGraph(attributePaths = { "association", "roles", "defaultRole" })
    List<Post> findByAssociationIdAndRequiresElectionTrueAndIsActiveTrue(Long associationId);

    /**
     * Trouve les postes par durée de mandat
     */
    @Query("SELECT p FROM Post p WHERE p.association.id = :associationId " +
            "AND p.mandateDurationMonths = :durationMonths")
    List<Post> findByAssociationIdAndMandateDuration(
            @Param("associationId") Long associationId,
            @Param("durationMonths") Integer durationMonths
    );
}
