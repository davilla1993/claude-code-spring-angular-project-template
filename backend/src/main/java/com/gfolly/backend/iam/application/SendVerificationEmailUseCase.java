package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.EmailVerificationToken;
import com.gfolly.backend.iam.infrastructure.email.EmailService;
import com.gfolly.backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SendVerificationEmailUseCase {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 15;

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(String userId, String email, String firstName) {
        tokenRepository.deleteAllByUserId(userId);

        String code = generateCode();

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(userId);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        tokenRepository.save(token);

        emailService.sendVerificationCode(email, firstName, code);
    }

    private String generateCode() {
        int num = new SecureRandom().nextInt(900_000) + 100_000;
        return String.valueOf(num);
    }
}

