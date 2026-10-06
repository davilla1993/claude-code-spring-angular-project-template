package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
