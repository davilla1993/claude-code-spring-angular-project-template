package com.gfolly.quantly_backend.iam.infrastructure.repository;

import com.gfolly.quantly_backend.iam.domain.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByUserIdOrderByCreatedAtDesc(String userId);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
