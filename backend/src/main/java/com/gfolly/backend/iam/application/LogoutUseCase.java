package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ferme la session courante (identifiée par son refresh token). Un token d'une session déjà remplacée
 * est simplement révoqué : il ne ferme pas la session ouverte depuis.
 */
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(rawRefreshToken))
                .ifPresent(this::close);
    }

    private void close(RefreshToken token) {
        token.setRevoked(true);
        userRepository.findByPublicIdAndDeletedFalse(token.getUserId())
                .filter(user -> token.belongsToSession(user.getCurrentSessionId()))
                .ifPresent(user -> user.setCurrentSessionId(null));
    }
}
