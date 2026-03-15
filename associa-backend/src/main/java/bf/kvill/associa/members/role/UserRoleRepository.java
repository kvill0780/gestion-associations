package bf.kvill.associa.members.role;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role r JOIN FETCH r.association WHERE ur.userId = :userId AND ur.isActive = true")
    List<UserRole> findByUserIdAndIsActiveTrueWithRole(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role r JOIN FETCH r.association " +
            "WHERE ur.userId = :userId " +
            "AND ur.isActive = true " +
            "AND (ur.termStart IS NULL OR ur.termStart <= :today) " +
            "AND (ur.termEnd IS NULL OR ur.termEnd >= :today)")
    List<UserRole> findCurrentByUserIdWithRole(
            @Param("userId") Long userId,
            @Param("today") LocalDate today);

    List<UserRole> findByRoleId(Long roleId);

    List<UserRole> findByRoleIdAndIsActiveTrue(Long roleId);

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    Optional<UserRole> findByUserIdAndRoleIdAndIsActiveTrue(Long userId, Long roleId);

    boolean existsByUserIdAndRoleIdAndIsActiveTrue(Long userId, Long roleId);

    long countByRoleIdAndIsActiveTrue(Long roleId);

    List<UserRole> findByAssignedById(Long assignedById);

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.role r JOIN FETCH r.association WHERE ur.isActive = true")
    List<UserRole> findAllActiveWithRole();
}
