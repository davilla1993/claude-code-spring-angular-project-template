package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.EmailVerificationToken;
import com.gfolly.backend.iam.infrastructure.email.EmailService;
import com.gfolly.backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendVerificationEmailUseCase {

    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    /**
     * REQUIRES_NEW : le code doit être persisté même si l'appelant échoue ensuite
     * (le login lève EmailNotVerifiedException juste après cet appel).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(String userId, String email, String firstName) {
        tokenRepository.deleteAllByUserId(userId);

        String code = OneTimeCodes.generate();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUserId(userId);
        token.setCode(code);
        token.setExpiresAt(OneTimeCodes.expiry());
        tokenRepository.save(token);

        emailService.sendVerificationCode(email, firstName, code);
    }
}
