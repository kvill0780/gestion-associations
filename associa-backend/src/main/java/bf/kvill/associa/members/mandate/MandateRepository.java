package bf.kvill.associa.members.mandate;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Mandate
 */
@Repository
public interface MandateRepository extends JpaRepository<Mandate, Long> {

        @EntityGraph(attributePaths = {"user", "post", "association"})
        @Query("SELECT m FROM Mandate m WHERE m.id = :id")
        Optional<Mandate> findByIdWithDetails(@Param("id") Long id);

        long countByPostIdAndActiveTrue(Long postId);

        long countByPostId(Long postId);

        long countByAssociationIdAndActiveTrue(Long associationId);

        /**
         * Recherche les mandats d'un utilisateur
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByUserId(Long userId);

        /**
         * Recherche les mandats actifs d'un utilisateur
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByUserIdAndActiveTrue(Long userId);

        /**
         * Recherche les mandats d'un poste
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByPostId(Long postId);

        /**
         * Recherche le mandat actif d'un poste
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByPostIdAndActiveTrue(Long postId);

        @EntityGraph(attributePaths = {"user", "post", "association"})
        @Query("SELECT m FROM Mandate m WHERE m.post.id = :postId " +
                        "AND m.active = true " +
                        "AND m.startDate <= :currentDate " +
                        "AND (m.endDate IS NULL OR m.endDate >= :currentDate)")
        List<Mandate> findCurrentByPostId(
                        @Param("postId") Long postId,
                        @Param("currentDate") LocalDate currentDate);

        Optional<Mandate> findByUserIdAndPostIdAndActiveTrue(Long userId, Long postId);

        /**
         * Recherche les mandats d'une association
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByAssociationId(Long associationId);

        /**
         * Recherche les mandats actifs d'une association
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByAssociationIdAndActiveTrue(Long associationId);

        /**
         * Recherche les mandats actifs d'un utilisateur pour une association
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        List<Mandate> findByUserIdAndAssociationIdAndActiveTrue(Long userId, Long associationId);

        /**
         * Compter mandats actifs
         */
        @Query("SELECT COUNT(m) FROM Mandate m WHERE m.post.id = :postId AND m.active = true")
        long countActiveByPostId(@Param("postId") Long postId);

        @Query("SELECT COUNT(m) FROM Mandate m " +
                        "WHERE m.post.id = :postId " +
                        "AND m.active = true " +
                        "AND m.startDate <= :currentDate " +
                        "AND (m.endDate IS NULL OR m.endDate >= :currentDate)")
        long countCurrentActiveByPostId(
                        @Param("postId") Long postId,
                        @Param("currentDate") LocalDate currentDate);

        @Query("SELECT COUNT(m) FROM Mandate m " +
                        "WHERE m.user.id = :userId " +
                        "AND m.active = true " +
                        "AND m.startDate <= :currentDate " +
                        "AND (m.endDate IS NULL OR m.endDate >= :currentDate)")
        long countCurrentActiveByUserId(
                        @Param("userId") Long userId,
                        @Param("currentDate") LocalDate currentDate);

        /**
         * Recherche les mandats en cours (actifs et non expirés)
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        @Query("SELECT m FROM Mandate m WHERE m.active = true AND (m.endDate IS NULL OR m.endDate >= :currentDate)")
        List<Mandate> findCurrentMandates(@Param("currentDate") LocalDate currentDate);

        /**
         * Recherche les mandats en cours pour une association
         */
        @EntityGraph(attributePaths = {"user", "post", "association"})
        @Query("SELECT m FROM Mandate m WHERE m.association.id = :associationId AND m.active = true AND m.startDate <= :currentDate AND (m.endDate IS NULL OR m.endDate >= :currentDate)")
        List<Mandate> findCurrentMandatesByAssociation(@Param("associationId") Long associationId,
                        @Param("currentDate") LocalDate currentDate);

        /**
         * Vérifie si un utilisateur a un mandat actif sur un poste
         */
        boolean existsByUserIdAndPostIdAndActiveTrue(Long userId, Long postId);

        boolean existsByPostIdAndActiveTrue(Long postId);

        long countByUserIdAndActiveTrue(Long userId);

}
