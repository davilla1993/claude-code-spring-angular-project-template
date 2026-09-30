package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Définit le mot de passe lors du premier login (firstLogin = true).
 * Aucune vérification du mot de passe actuel : le JWT suffit à prouver l'identité,
 * et l'utilisateur a déjà prouvé qu'il connaît le mot de passe temporaire en se connectant.
 */
@Service
@RequiredArgsConstructor
public class SetupPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String userId, String newPassword) {
        User user = userRepository.findByPublicIdGlobal(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!Boolean.TRUE.equals(user.getFirstLogin())) {
            throw new IllegalStateException(ErrorMessages.PASSWORD_ALREADY_SET);
        }

        String newHash = passwordEncoder.encode(newPassword);

        if (user.getRole() == com.gfolly.backend.iam.domain.Role.OWNER) {
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

