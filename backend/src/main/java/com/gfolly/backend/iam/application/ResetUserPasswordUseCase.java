package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Un administrateur génère un mot de passe temporaire pour un utilisateur.
 * L'utilisateur devra le remplacer à sa prochaine connexion ; ses sessions sont révoquées.
 */
@Service
@RequiredArgsConstructor
public class ResetUserPasswordUseCase {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*";
    private static final int LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    /**
     * @return le mot de passe temporaire en clair, à transmettre à l'utilisateur
     */
    @Transactional
    public String execute(String targetId) {
        User user = userRepository.findByPublicIdAndDeletedFalse(targetId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String temporaryPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setFirstLogin(true);
        refreshTokenRepository.revokeAllByUserId(targetId);

        auditLogService.log("USER_PASSWORD_RESET", "USER", targetId, null, AuditLog.ActionStatus.SUCCESS);
        return temporaryPassword;
    }

    private static String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
