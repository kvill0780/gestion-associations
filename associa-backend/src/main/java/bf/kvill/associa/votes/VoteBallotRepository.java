package bf.kvill.associa.votes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteBallotRepository extends JpaRepository<VoteBallot, Long> {
    long countByVoteId(Long voteId);

    long countByOptionId(Long optionId);

    long countByVoteIdAndUserId(Long voteId, Long userId);

    boolean existsByVoteIdAndUserId(Long voteId, Long userId);

    boolean existsByVoteIdAndUserIdAndOptionId(Long voteId, Long userId, Long optionId);
}
