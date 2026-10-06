package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rotation du refresh token : l'ancien est révoqué, une nouvelle paire est émise.
 * La présentation d'un token déjà révoqué est traitée comme un vol : toutes les sessions
 * de l'utilisateur sont révoquées.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthTokenService authTokenService;

    // noRollbackFor : la révocation en cas de réutilisation doit être conservée malgré l'exception.
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public AuthSession execute(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }

        RefreshToken existing = refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(rawRefreshToken))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        if (existing.isRevoked()) {
            log.warn("Revoked refresh token reused for user {} — revoking all sessions", existing.getUserId());
            refreshTokenRepository.revokeAllByUserId(existing.getUserId());
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        if (existing.isExpired()) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }

        User user = userRepository.findByPublicIdAndDeletedFalse(existing.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        existing.setRevoked(true);
        return new AuthSession(authTokenService.issueTokens(user), UserResponseMapper.toResponse(user));
    }
}
