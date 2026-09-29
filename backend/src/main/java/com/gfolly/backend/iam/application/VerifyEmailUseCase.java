package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.EmailVerificationToken;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.EmailAlreadyVerifiedException;
import com.gfolly.quantly_backend.iam.domain.exception.InvalidVerificationCodeException;
import com.gfolly.quantly_backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
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
        User user = userRepository.findByPublicIdGlobal(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailAlreadyVerifiedException(ErrorMessages.EMAIL_ALREADY_VERIFIED);
        }

        EmailVerificationToken token = tokenRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new InvalidVerificationCodeException(ErrorMessages.LINK_EXPIRED_OR_INVALID));

        if (!token.isValid()) {
            throw new InvalidVerificationCodeException(ErrorMessages.LINK_RESEND_REQUIRED);
        }

        if (!token.getCode().equals(code)) {
            throw new InvalidVerificationCodeException(ErrorMessages.LINK_EXPIRED_OR_INVALID);
        }

        token.setUsed(true);
        userRepository.markEmailVerified(user.getPublicId());
    }
}
