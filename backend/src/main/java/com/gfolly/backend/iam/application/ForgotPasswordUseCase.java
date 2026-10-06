package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.PasswordResetToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.email.EmailService;
import com.gfolly.backend.iam.infrastructure.repository.PasswordResetTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * Réponse identique quelle que soit l'issue : ne révèle pas l'existence du compte.
     */
    @Transactional
    public void execute(String email) {
        userRepository.findByEmailAndDeletedFalse(User.normalizeEmail(email))
                .filter(User::isActive)
                .ifPresent(this::sendResetCode);
    }

    private void sendResetCode(User user) {
        tokenRepository.deleteAllByUserId(user.getPublicId());

        String code = OneTimeCodes.generate();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getPublicId());
        token.setCode(code);
        token.setExpiresAt(OneTimeCodes.expiry());
        tokenRepository.save(token);

        emailService.sendPasswordResetCode(user.getEmail(), user.getFirstName(), code);
    }
}
