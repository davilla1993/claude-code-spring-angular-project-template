package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ResetEmployeePasswordUseCase {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
    private static final int LENGTH = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Génère un mot de passe temporaire pour l'employé, force un changement au prochain login.
     * Réservé aux OWNER/MANAGER via la permission user:update.
     *
     * @return le mot de passe temporaire en clair (à communiquer à l'employé)
     */
    @Transactional
    public String execute(String employeePublicId) {
        User employee = userRepository.findByPublicId(employeePublicId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Sécurité : on ne peut pas réinitialiser le mot de passe d'un OWNER via cet endpoint
        if (employee.getRole() == Role.OWNER) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        String tempPassword = generateTempPassword();
        userRepository.updatePasswordHashAndFirstLogin(
                employeePublicId,
                passwordEncoder.encode(tempPassword),
                true
        );

        return tempPassword;
    }

    private String generateTempPassword() {
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARS.charAt(rng.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}

