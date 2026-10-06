package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Révoque la session courante (identifiée par son refresh token). Les autres appareils restent connectés.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(rawRefreshToken))
                .ifPresent(token -> token.setRevoked(true));
    }
}
