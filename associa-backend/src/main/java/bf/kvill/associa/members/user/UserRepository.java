package bf.kvill.associa.members.user;

import bf.kvill.associa.shared.enums.MembershipStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Charge user + userRoles pour Spring Security
     */
    @EntityGraph(attributePaths = { "userRoles", "userRoles.role" })
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = { "association", "userRoles", "userRoles.role" })
    List<User> findAll();

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = { "association", "userRoles", "userRoles.role" })
    List<User> findByAssociationId(Long associationId);

    List<User> findByMembershipStatus(MembershipStatus status);

    @EntityGraph(attributePaths = { "association" })
    List<User> findByAssociationIdAndMembershipStatus(Long associationId, MembershipStatus status);

    List<User> findByIsSuperAdminTrue();

    @Query("SELECT u FROM User u WHERE u.emailVerifiedAt IS NOT NULL")
    List<User> findVerifiedUsers();

    @Query("SELECT u FROM User u WHERE u.association.id = :associationId AND u.emailVerifiedAt IS NOT NULL")
    List<User> findVerifiedUsersByAssociation(@Param("associationId") Long associationId);

    @EntityGraph(attributePaths = { "association" })
    Page<User> findByAssociationId(Long associationId, Pageable pageable);

    @EntityGraph(attributePaths = { "association" })
    Page<User> findByAssociationIdAndMembershipStatus(Long associationId, MembershipStatus status, Pageable pageable);

    long countByAssociationIdAndMembershipStatus(Long associationId, MembershipStatus status);

    long countByAssociationId(Long associationId);

    long countByAssociationIdAndMembershipDateBetween(Long associationId, LocalDate start, LocalDate end);

    long countByAssociationIdAndMembershipStatusAndMembershipDateBefore(
            Long associationId, MembershipStatus status, LocalDate date);

    long countByAssociationIdAndMembershipDateGreaterThanEqual(Long associationId, LocalDate date);

    @EntityGraph(attributePaths = { "association", "userRoles", "userRoles.role" })
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithUserRoles(@Param("email") String email);

    @EntityGraph(attributePaths = { "association", "userRoles", "userRoles.role" })
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithUserRoles(@Param("id") Long id);

}
