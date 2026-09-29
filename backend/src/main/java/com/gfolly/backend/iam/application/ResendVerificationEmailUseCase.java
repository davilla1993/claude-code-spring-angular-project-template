package com.gfolly.quantly_backend.iam.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.EmailAlreadyVerifiedException;
import com.gfolly.quantly_backend.iam.domain.exception.TooManyRequestsException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailUseCase {

    private static final int MAX_RESENDS   = 4;
    private static final int WINDOW_HOURS  = 1;

    /** Clé : email (lowercase) → nombre de renvois sur la fenêtre glissante d'1h. */
    private final Cache<String, AtomicInteger> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW_HOURS, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    private final UserRepository userRepository;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    @Transactional
    public void execute(String email) {
        String key = email.toLowerCase();
        AtomicInteger counter = rateLimitCache.get(key, k -> new AtomicInteger(0));

        if (counter.incrementAndGet() > MAX_RESENDS) {
            throw new TooManyRequestsException(ErrorMessages.tooManyResends(MAX_RESENDS));
        }

        Optional<User> userOpt = userRepository.findByEmailGlobal(email);

        // Silently succeed if email not found (don't reveal account existence)
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new EmailAlreadyVerifiedException(ErrorMessages.EMAIL_ALREADY_VERIFIED);
        }

        sendVerificationEmailUseCase.execute(user.getPublicId(), user.getEmail(), user.getFirstName());
    }
}
