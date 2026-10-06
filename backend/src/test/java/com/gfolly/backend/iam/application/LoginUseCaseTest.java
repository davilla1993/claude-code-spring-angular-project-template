package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailNotVerifiedException;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginUseCaseTest {

    private static final String PASSWORD = "Secret#123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final SendVerificationEmailUseCase sendVerificationEmail = mock(SendVerificationEmailUseCase.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);

    private LoginUseCase loginUseCase;
    private User user;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(userRepository, passwordEncoder, authTokenService, sendVerificationEmail,
                auditLogService);

        user = new User();
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.USER);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setEmailVerified(true);
    }

    @Test
    void successfulLoginIssuesTokensAndNormalizesEmail() {
        when(userRepository.findByEmailAndDeletedFalse("jane@example.com")).thenReturn(Optional.of(user));
        when(authTokenService.issueTokens(user)).thenReturn(new TokenPair("access", "refresh"));

        AuthSession session = loginUseCase.execute(new LoginRequest("  Jane@Example.COM ", PASSWORD));

        assertThat(session.tokens().accessToken()).isEqualTo("access");
        assertThat(session.user().email()).isEqualTo("jane@example.com");
        verify(auditLogService).logAs(eq("jane@example.com"), eq("LOGIN"), any(), any(), any(),
                eq(AuditLog.ActionStatus.SUCCESS));
    }

    @Test
    void unknownEmailIsRejectedAndAudited() {
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginUseCase.execute(new LoginRequest("nobody@example.com", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(ErrorMessages.CREDENTIALS_INVALID);
        verify(auditLogService).logAs(eq("nobody@example.com"), eq("LOGIN"), any(), isNull(), any(),
                eq(AuditLog.ActionStatus.FAILED));
        verifyNoInteractions(authTokenService);
    }

    @Test
    void wrongPasswordIsRejected() {
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.execute(new LoginRequest("jane@example.com", "Wrong#123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(ErrorMessages.CREDENTIALS_INVALID);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void disabledAccountIsRevealedOnlyWithCorrectPassword() {
        user.setActive(false);
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.execute(new LoginRequest("jane@example.com", "Wrong#123")))
                .hasMessage(ErrorMessages.CREDENTIALS_INVALID);
        assertThatThrownBy(() -> loginUseCase.execute(new LoginRequest("jane@example.com", PASSWORD)))
                .hasMessage(ErrorMessages.ACCOUNT_DISABLED);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void unverifiedEmailSendsNewCodeAndBlocksLogin() {
        user.setEmailVerified(false);
        when(userRepository.findByEmailAndDeletedFalse(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> loginUseCase.execute(new LoginRequest("jane@example.com", PASSWORD)))
                .isInstanceOf(EmailNotVerifiedException.class)
                .extracting(e -> ((EmailNotVerifiedException) e).getUserId())
                .isEqualTo(user.getPublicId());
        verify(sendVerificationEmail).execute(user.getPublicId(), user.getEmail(), user.getFirstName());
        verifyNoInteractions(authTokenService);
    }
}
