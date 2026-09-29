package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.PasswordResetToken;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.InvalidVerificationCodeException;
import com.gfolly.quantly_backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.PasswordResetTokenRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String email, String code, String newPassword) {
        User user = userRepository.findByEmailGlobal(email)
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.LINK_EXPIRED_OR_INVALID));

        PasswordResetToken token = tokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getPublicId())
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.LINK_EXPIRED_OR_INVALID));

        if (!token.isValid()) {
            throw new InvalidVerificationCodeException(ErrorMessages.LINK_RESEND_REQUIRED);
        }

        if (!token.getCode().equals(code)) {
            throw new InvalidVerificationCodeException(ErrorMessages.LINK_EXPIRED_OR_INVALID);
        }

        token.setUsed(true);
        String newHash = passwordEncoder.encode(newPassword);

        if (user.getRole() == com.gfolly.quantly_backend.iam.domain.Role.OWNER || user.getRole() == com.gfolly.quantly_backend.iam.domain.Role.COMMANDER) {
            userRepository.updatePasswordHashGlobal(email, newHash);
        } else {
            userRepository.updatePasswordHash(user.getPublicId(), newHash);
        }
    }
}
