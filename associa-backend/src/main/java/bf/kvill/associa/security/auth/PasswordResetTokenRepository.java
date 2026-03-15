package bf.kvill.associa.security.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.token = :token AND prt.used = false AND prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.user.id = :userId ORDER BY prt.createdAt DESC")
    Optional<PasswordResetToken> findLatestByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user.id = :userId AND prt.used = false")
    void invalidateAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :now OR prt.used = true")
    void deleteExpiredOrUsedTokens(@Param("now") LocalDateTime now);

    boolean existsByTokenAndUsedFalse(String token);
}
