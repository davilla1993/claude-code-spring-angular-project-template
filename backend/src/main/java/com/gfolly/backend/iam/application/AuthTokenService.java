package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.security.JwtService;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Émet une nouvelle session : access token JWT + refresh token opaque (persisté sous forme hachée).
 * Chaque appel crée une session indépendante (multi-appareils).
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

    @Transactional
    public TokenPair issueTokens(User user) {
        String rawRefreshToken = OpaqueTokens.generate();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(OpaqueTokens.hash(rawRefreshToken));
        refreshToken.setUserId(user.getPublicId());
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(jwtService.generateAccessToken(user), rawRefreshToken);
    }
}
