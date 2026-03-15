package bf.kvill.associa.announcements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByAssociationIdOrderByCreatedAtDesc(Long associationId);

    Optional<Announcement> findByIdAndAssociationId(Long id, Long associationId);
}
