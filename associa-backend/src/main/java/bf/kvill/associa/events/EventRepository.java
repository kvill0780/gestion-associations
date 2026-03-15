package bf.kvill.associa.events;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Événements d'une association
    Page<Event> findByAssociationId(Long associationId, Pageable pageable);

    // Événements d'une association par statut
    Page<Event> findByAssociationIdAndStatus(Long associationId, bf.kvill.associa.events.enums.EventStatus status,
            Pageable pageable);

    // Événements à venir (après une date)
    @Query("SELECT e FROM Event e WHERE e.association.id = :associationId AND e.startDate >= :date ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(
            @Param("associationId") Long associationId,
            @Param("date") LocalDateTime date,
            Pageable pageable);

    // Événements passés (avant une date)
    @Query("SELECT e FROM Event e WHERE e.association.id = :associationId AND e.endDate < :date ORDER BY e.endDate DESC")
    List<Event> findPastEvents(
            @Param("associationId") Long associationId,
            @Param("date") LocalDateTime date,
            Pageable pageable);

    long countByAssociationId(Long associationId);
}
