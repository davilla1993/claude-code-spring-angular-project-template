package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Role;
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
 * La présentation d'un token déjà révoqué de la session ouverte est traitée comme un vol : la session est fermée.
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

        User user = userRepository.findByPublicIdAndDeletedFalse(existing.getUserId())
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        // Session remplacée ou fermée : simple refus. Ce n'est pas un vol, et la session ouverte ne doit pas en pâtir.
        if (!existing.belongsToSession(user.getCurrentSessionId())) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        if (existing.isRevoked()) {
            log.warn("Revoked refresh token reused for user {} — closing the session", existing.getUserId());
            refreshTokenRepository.revokeAllByUserId(existing.getUserId());
            user.setCurrentSessionId(null);
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        if (existing.isExpired()) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }

        existing.setRevoked(true);
        Role activeRole = resolveActiveRole(existing, user);
        return new AuthSession(authTokenService.issueTokens(user, activeRole),
                UserResponseMapper.toResponse(user, activeRole));
    }

    /** Conserve le profil de la session, à condition que l'utilisateur détienne toujours ce rôle. */
    private static Role resolveActiveRole(RefreshToken token, User user) {
        Role activeRole = token.getActiveRole();
        return activeRole != null && user.getRoles().contains(activeRole)
                ? activeRole
                : user.soleRole().orElse(null);
    }
}
