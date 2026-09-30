package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Tenant;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.mapper.UserMapper;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.JwtService;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthSessionResult execute(String rawRefreshToken) {
        RefreshToken existing = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        if (!existing.isValid()) {
            throw new InvalidCredentialsException(
                    existing.isExpired() ? ErrorMessages.TOKEN_EXPIRED : ErrorMessages.TOKEN_REVOKED);
        }

        User user = userRepository.findByPublicId(existing.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Tenant tenant = tenantRepository.findByPublicId(existing.getTenantId()).orElse(null);
        String tenantSlug    = tenant != null ? tenant.getSlug()    : "";
        String tenantName    = tenant != null ? tenant.getName()    : null;
        String tenantLogoUrl = tenant != null ? tenant.getLogoUrl() : null;
        String plan          = tenant != null ? tenant.getPlan()    : null;

        // Rotation : révoquer l'ancien token
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        String newAccessToken = jwtService.generateAccessToken(user, tenantSlug);
        String newRefreshTokenValue = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenValue);
        newRefreshToken.setUserId(user.getPublicId());
        newRefreshToken.setTenantId(existing.getTenantId());
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshTokenRepository.save(newRefreshToken);

        return new AuthSessionResult(
                new TokenPair(newAccessToken, newRefreshTokenValue),
                new AuthResponse(UserMapper.toResponse(user, tenantSlug, tenantName, tenantLogoUrl, plan))
        );
    }
}


