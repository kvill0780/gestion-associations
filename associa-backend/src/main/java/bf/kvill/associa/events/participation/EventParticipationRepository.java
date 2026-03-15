package bf.kvill.associa.events.participation;

import bf.kvill.associa.shared.enums.EventParticipationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité EventParticipation
 */
@Repository
public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {

    /**
     * Recherche les participations à un événement
     */
    @EntityGraph(attributePaths = { "user" })
    List<EventParticipation> findByEventIdOrderByRegisteredAtDesc(Long eventId);

    /**
     * Recherche les participations d'un utilisateur
     */
    List<EventParticipation> findByUserId(Long userId);

    /**
     * Recherche une participation spécifique
     */
    Optional<EventParticipation> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Recherche les participations par statut
     */
    List<EventParticipation> findByEventIdAndStatus(Long eventId, EventParticipationStatus status);

    /**
     * Vérifie si un utilisateur est inscrit à un événement
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Compte les participants à un événement
     */
    long countByEventId(Long eventId);

    /**
     * Compte les participants par statut
     */
    long countByEventIdAndStatus(Long eventId, EventParticipationStatus status);

    /**
     * Compte les participants par lot de statuts
     */
    long countByEventIdAndStatusIn(Long eventId, Collection<EventParticipationStatus> statuses);
}
