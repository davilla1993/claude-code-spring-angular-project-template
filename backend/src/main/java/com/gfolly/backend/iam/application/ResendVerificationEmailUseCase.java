package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyVerifiedException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Renvoie un code de vérification. Volume limité par RateLimitFilter (par email).
 */
@Service
@RequiredArgsConstructor
public class ResendVerificationEmailUseCase {

    private final UserRepository userRepository;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    @Transactional(readOnly = true)
    public void execute(String email) {
        // Email inconnu : succès silencieux pour ne pas révéler l'existence du compte.
        userRepository.findByEmailAndDeletedFalse(User.normalizeEmail(email)).ifPresent(user -> {
            if (user.isEmailVerified()) {
                throw new EmailAlreadyVerifiedException(ErrorMessages.EMAIL_ALREADY_VERIFIED);
            }
            sendVerificationEmailUseCase.execute(user.getPublicId(), user.getEmail(), user.getFirstName());
        });
    }
}
