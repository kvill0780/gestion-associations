package bf.kvill.associa.votes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByAssociationIdOrderByCreatedAtDesc(Long associationId);

    Optional<Vote> findByIdAndAssociationId(Long id, Long associationId);
}
