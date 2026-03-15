package bf.kvill.associa.announcements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementPollOptionRepository extends JpaRepository<AnnouncementPollOption, Long> {
    List<AnnouncementPollOption> findByAnnouncementId(Long announcementId);
}
