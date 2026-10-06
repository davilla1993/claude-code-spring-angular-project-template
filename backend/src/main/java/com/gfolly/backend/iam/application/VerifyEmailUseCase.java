package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.EmailVerificationToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyVerifiedException;
import com.gfolly.backend.iam.domain.exception.InvalidVerificationCodeException;
import com.gfolly.backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;

    @Transactional
    public void execute(String userId, String code) {
        User user = userRepository.findByPublicIdAndDeletedFalse(userId)
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID));

        if (user.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(ErrorMessages.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerificationToken token = tokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID));

        if (!token.isValid()) {
            throw new InvalidVerificationCodeException(ErrorMessages.CODE_EXPIRED);
        }
        if (!token.matches(code)) {
            throw new InvalidVerificationCodeException(ErrorMessages.CODE_INVALID);
        }

        token.setUsed(true);
        user.setEmailVerified(true);
    }
}
