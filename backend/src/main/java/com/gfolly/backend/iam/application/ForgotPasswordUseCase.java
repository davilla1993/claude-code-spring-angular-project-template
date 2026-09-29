package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.PasswordResetToken;
import com.gfolly.quantly_backend.iam.domain.Role;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.infrastructure.email.EmailService;
import com.gfolly.quantly_backend.iam.infrastructure.repository.PasswordResetTokenRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private static final int EXPIRY_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void execute(String email) {
        // Réponse générique quelle que soit l'issue (ne révèle pas l'existence du compte)
        Optional<User> userOpt = userRepository.findByEmailGlobal(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        // Réinitialisation disponible uniquement via le portail — OWNER et COMMANDER seulement
        if (user.getRole() != Role.OWNER && user.getRole() != Role.COMMANDER) return;
        if (!Boolean.TRUE.equals(user.getActive())) return;

        tokenRepository.deleteAllByUserId(user.getPublicId());

        String code = generateCode();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getPublicId());
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        tokenRepository.save(token);

        emailService.sendPasswordResetCode(user.getEmail(), user.getFirstName(), code);
    }

    private String generateCode() {
        return String.valueOf(new SecureRandom().nextInt(900_000) + 100_000);
    }
}
