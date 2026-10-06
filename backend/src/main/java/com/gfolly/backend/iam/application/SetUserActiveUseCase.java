package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Active ou désactive un compte. La désactivation révoque toutes les sessions de l'utilisateur.
 */
@Service
@RequiredArgsConstructor
public class SetUserActiveUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;

    /**
     * @param actorId publicId de l'utilisateur qui effectue l'opération
     */
    @Transactional
    public void execute(String actorId, String targetId, boolean active) {
        if (!active && actorId.equals(targetId)) {
            throw new ForbiddenOperationException(ErrorMessages.CANNOT_DEACTIVATE_SELF);
        }

        User user = userRepository.findByPublicIdAndDeletedFalse(targetId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setActive(active);
        if (!active) {
            refreshTokenRepository.revokeAllByUserId(targetId);
        }

        auditLogService.log(active ? "USER_ACTIVATE" : "USER_DEACTIVATE", "USER", targetId, null,
                AuditLog.ActionStatus.SUCCESS);
    }
}
