package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.ResetPasswordRequest;
import com.gfolly.backend.iam.domain.PasswordResetToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidVerificationCodeException;
import com.gfolly.backend.iam.infrastructure.repository.PasswordResetTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Réinitialisation via le code reçu par email. Toutes les sessions existantes sont révoquées.
 */
@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(User.normalizeEmail(request.email()))
                .filter(User::isActive)
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID));

        PasswordResetToken token = tokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getPublicId())
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID));

        if (!token.isValid()) {
            throw new InvalidVerificationCodeException(ErrorMessages.CODE_EXPIRED);
        }
        if (!token.matches(request.code())) {
            throw new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID);
        }

        token.setUsed(true);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFirstLogin(false);
        refreshTokenRepository.revokeAllByUserId(user.getPublicId());

        auditLogService.logAs(user.getEmail(), "PASSWORD_RESET", "USER", user.getPublicId(), null,
                AuditLog.ActionStatus.SUCCESS);
    }
}
