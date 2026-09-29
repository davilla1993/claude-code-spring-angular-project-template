package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.system.infrastructure.service.AuditLogService;
import com.gfolly.quantly_backend.system.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeactivateUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(String userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setActive(false);
        userRepository.save(user);

        // Invalider toutes les sessions actives
        refreshTokenRepository.revokeAllByUserId(userPublicId);

        auditLogService.log("USER_DEACTIVATE", "USER", user.getPublicId(),
                "Désactivation de l'utilisateur " + user.getEmail(),
                AuditLog.ActionStatus.SUCCESS);
    }
}

