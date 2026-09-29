package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.quantly_backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByPublicIdGlobal(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorMessages.CURRENT_PASSWORD_INCORRECT);
        }

        String newHash = passwordEncoder.encode(newPassword);

        if (user.getRole() == com.gfolly.quantly_backend.iam.domain.Role.OWNER) {
            userRepository.updatePasswordHashGlobal(user.getEmail(), newHash);
        } else {
            userRepository.updatePasswordHashAndFirstLogin(
                    userId,
                    newHash,
                    false
            );
        }
    }
}
