package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Choisit ou change le profil actif de la session courante : le rôle doit appartenir à l'utilisateur,
 * la session courante est remplacée par une nouvelle, sur ce profil : ses anciens tokens sont aussitôt refusés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SelectProfileUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenService authTokenService;
    private final AuditLogService auditLogService;

    // noRollbackFor : la révocation en cas de réutilisation doit être conservée malgré l'exception.
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public AuthSession execute(String userId, String rawRefreshToken, Role role) {
        User user = userRepository.findByPublicIdAndDeletedFalse(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        if (!user.getRoles().contains(role)) {
            auditLogService.log("PROFILE_SELECT", "USER", userId, "profile=" + role, AuditLog.ActionStatus.FAILED);
            throw new ForbiddenOperationException(ErrorMessages.PROFILE_NOT_HELD);
        }

        currentSession(user, rawRefreshToken).setRevoked(true);
        AuthSession session = new AuthSession(authTokenService.startExclusiveSession(user, role),
                UserResponseMapper.toResponse(user, role));

        auditLogService.log("PROFILE_SELECT", "USER", userId, "profile=" + role, AuditLog.ActionStatus.SUCCESS);
        return session;
    }

    private RefreshToken currentSession(User user, String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        RefreshToken token = refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(rawRefreshToken))
                .filter(candidate -> candidate.getUserId().equals(user.getPublicId()))
                .filter(candidate -> candidate.belongsToSession(user.getCurrentSessionId()))
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID));

        // Même traitement qu'au refresh : présenter un token déjà révoqué est traité comme un vol.
        if (token.isRevoked()) {
            log.warn("Revoked refresh token reused for user {} — closing the session", user.getPublicId());
            refreshTokenRepository.revokeAllByUserId(user.getPublicId());
            user.setCurrentSessionId(null);
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        if (token.isExpired()) {
            throw new InvalidCredentialsException(ErrorMessages.TOKEN_INVALID);
        }
        return token;
    }
}
