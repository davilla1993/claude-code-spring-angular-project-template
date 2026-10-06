package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remplace le mot de passe temporaire lors de la première connexion (firstLogin = true).
 * Le mot de passe actuel n'est pas redemandé : l'utilisateur vient de s'authentifier avec.
 */
@Service
@RequiredArgsConstructor
public class SetupPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(String userId, String newPassword) {
        User user = userRepository.findByPublicIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!user.isFirstLogin()) {
            throw new IllegalStateException(ErrorMessages.PASSWORD_ALREADY_SET);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);

        auditLogService.log("PASSWORD_SETUP", "USER", userId, null, AuditLog.ActionStatus.SUCCESS);
    }
}
