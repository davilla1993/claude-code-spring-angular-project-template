package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidPasswordException;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByPublicIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException(ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);

        auditLogService.log("PASSWORD_CHANGE", "USER", userId, null, AuditLog.ActionStatus.SUCCESS);
    }
}
