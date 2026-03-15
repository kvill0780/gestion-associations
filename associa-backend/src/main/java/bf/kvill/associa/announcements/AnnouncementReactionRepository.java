package bf.kvill.associa.announcements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementReactionRepository extends JpaRepository<AnnouncementReaction, Long> {

    long countByAnnouncementIdAndType(Long announcementId, AnnouncementReactionType type);

    Optional<AnnouncementReaction> findByAnnouncementIdAndUserId(Long announcementId, Long userId);

    void deleteByAnnouncementIdAndUserId(Long announcementId, Long userId);
}
