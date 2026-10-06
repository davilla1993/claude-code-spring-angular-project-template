package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.EmailVerificationToken;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyVerifiedException;
import com.gfolly.backend.iam.domain.exception.InvalidVerificationCodeException;
import com.gfolly.backend.iam.infrastructure.repository.EmailVerificationTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifyEmailUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final EmailVerificationTokenRepository tokenRepository = mock(EmailVerificationTokenRepository.class);
    private final VerifyEmailUseCase useCase = new VerifyEmailUseCase(userRepository, tokenRepository);

    private User user;
    private EmailVerificationToken token;

    @BeforeEach
    void setUp() {
        user = new User();
        token = new EmailVerificationToken();
        token.setUserId(user.getPublicId());
        token.setCode("123456");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByPublicIdAndDeletedFalse(user.getPublicId())).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserIdOrderByCreatedAtDesc(user.getPublicId())).thenReturn(Optional.of(token));
    }

    @Test
    void correctCodeVerifiesEmailAndConsumesToken() {
        useCase.execute(user.getPublicId(), "123456");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isUsed()).isTrue();
    }

    @Test
    void wrongCodeIsRejected() {
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), "654321"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessage(ErrorMessages.CODE_INVALID);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void expiredOrUsedCodeIsRejected() {
        token.setUsed(true);

        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), "123456"))
                .isInstanceOf(InvalidVerificationCodeException.class)
                .hasMessage(ErrorMessages.CODE_EXPIRED);
    }

    @Test
    void alreadyVerifiedEmailIsRejected() {
        user.setEmailVerified(true);

        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), "123456"))
                .isInstanceOf(EmailAlreadyVerifiedException.class);
    }
}
