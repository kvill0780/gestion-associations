package bf.kvill.associa.documents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByAssociationIdOrderByCreatedAtDesc(Long associationId);

    List<Document> findTop5ByAssociationIdOrderByCreatedAtDesc(Long associationId);

    long countByAssociationId(Long associationId);

    Optional<Document> findByIdAndAssociationId(Long id, Long associationId);
}
