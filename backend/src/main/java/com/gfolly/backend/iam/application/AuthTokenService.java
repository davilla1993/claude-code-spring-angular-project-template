package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.security.JwtService;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Émet les tokens d'une session : access token JWT + refresh token opaque (persisté sous forme hachée).
 * Un utilisateur n'a qu'une seule session ouverte : en démarrer une ferme la précédente.
 */
@Service
public class AuthTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final long refreshTokenExpirationMs;

    public AuthTokenService(RefreshTokenRepository refreshTokenRepository,
                            JwtService jwtService,
                            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    /**
     * Ferme toute session existante de l'utilisateur et en ouvre une nouvelle.
     *
     * @param activeRole profil actif de la session ; null tant qu'aucun profil n'est choisi
     */
    @Transactional
    public TokenPair startExclusiveSession(User user, Role activeRole) {
        refreshTokenRepository.revokeAllByUserId(user.getPublicId());
        user.setCurrentSessionId(UUID.randomUUID().toString());
        return issueTokens(user, activeRole);
    }

    /**
     * Émet de nouveaux tokens pour la session en cours (rotation).
     *
     * @param activeRole profil actif de la session ; null tant qu'aucun profil n'est choisi
     */
    @Transactional
    public TokenPair issueTokens(User user, Role activeRole) {
        String rawRefreshToken = OpaqueTokens.generate();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(OpaqueTokens.hash(rawRefreshToken));
        refreshToken.setUserId(user.getPublicId());
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshToken.setActiveRole(activeRole);
        refreshToken.setSessionId(user.getCurrentSessionId());
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(jwtService.generateAccessToken(user, activeRole), rawRefreshToken);
    }
}
