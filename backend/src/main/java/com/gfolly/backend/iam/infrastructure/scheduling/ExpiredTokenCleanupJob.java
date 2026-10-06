package com.gfolly.backend.iam.infrastructure.scheduling;

import com.gfolly.backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.PasswordResetTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Purge périodique des tokens expirés (refresh, vérification email, réinitialisation).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Scheduled(cron = "${app.security.token-cleanup-cron}")
    @Transactional
    public void purgeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int refresh = refreshTokenRepository.deleteExpired(now);
        int verification = emailVerificationTokenRepository.deleteExpired(now);
        int reset = passwordResetTokenRepository.deleteExpired(now);
        log.info("Expired tokens purged — refresh: {}, email verification: {}, password reset: {}",
                refresh, verification, reset);
    }
}
