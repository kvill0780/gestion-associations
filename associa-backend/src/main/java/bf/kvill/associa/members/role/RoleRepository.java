package bf.kvill.associa.members.role;

import bf.kvill.associa.shared.enums.RoleType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @EntityGraph(attributePaths = { "association" })
    @Query("SELECT r FROM Role r WHERE r.id = :id")
    Optional<Role> findByIdWithAssociation(@Param("id") Long id);

    List<Role> findByAssociationId(Long associationId);

    Optional<Role> findBySlugAndAssociationId(String slug, Long associationId);

    List<Role> findByType(RoleType type);

    List<Role> findByAssociationIdAndIsTemplateTrue(Long associationId);

    List<Role> findByAssociationIdOrderByDisplayOrderAsc(Long associationId);

    boolean existsBySlugAndAssociationId(String slug, Long associationId);

    @Query("SELECT r FROM Role r WHERE r.association.id = :associationId AND r.type = 'LEADERSHIP'")
    List<Role> findLeadershipRolesByAssociation(@Param("associationId") Long associationId);
}
