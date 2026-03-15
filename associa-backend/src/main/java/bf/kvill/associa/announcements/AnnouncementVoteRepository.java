package bf.kvill.associa.announcements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementVoteRepository extends JpaRepository<AnnouncementVote, Long> {

    long countByOptionId(Long optionId);

    boolean existsByOptionIdAndUserId(Long optionId, Long userId);

    long countByOption_Announcement_IdAndUserId(Long announcementId, Long userId);

    List<AnnouncementVote> findByOption_Announcement_IdAndUserId(Long announcementId, Long userId);
}
