package com.gfolly.backend.iam.application.auth;

import com.gfolly.backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Tenant;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.mapper.UserMapper;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthSessionResult issueTokens(User user, Tenant tenant) {
        refreshTokenRepository.revokeAllByUserId(user.getPublicId());

        String accessToken = jwtService.generateAccessToken(user, tenant.getSlug());
        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenValue);
        refreshToken.setUserId(user.getPublicId());
        refreshToken.setTenantId(user.getTenantId());
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshTokenRepository.save(refreshToken);

        return new AuthSessionResult(
                new TokenPair(accessToken, refreshTokenValue),
                new AuthResponse(UserMapper.toResponse(user, tenant.getSlug(), tenant.getName(), tenant.getLogoUrl(), tenant.getPlan()))
        );
    }
}

