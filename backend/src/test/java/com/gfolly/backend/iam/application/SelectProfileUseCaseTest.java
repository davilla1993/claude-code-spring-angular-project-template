package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SelectProfileUseCaseTest {

    private static final String RAW_TOKEN = "raw-refresh-token";

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final SelectProfileUseCase useCase =
            new SelectProfileUseCase(userRepository, refreshTokenRepository, authTokenService, auditLogService);

    private User user;
    private RefreshToken stored;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.getRoles().add(Role.USER);

        user.setCurrentSessionId("session");

        stored = new RefreshToken();
        stored.setSessionId("session");
        stored.setTokenHash(OpaqueTokens.hash(RAW_TOKEN));
        stored.setUserId(user.getPublicId());
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(userRepository.findByPublicIdAndDeletedFalse(user.getPublicId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(RAW_TOKEN))).thenReturn(Optional.of(stored));
    }

    @Test
    void roleNotHeldIsForbiddenAndAudited() {
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), RAW_TOKEN, Role.ADMIN))
                .isInstanceOf(ForbiddenOperationException.class);

        assertThat(stored.isRevoked()).isFalse();
        verifyNoInteractions(authTokenService);
        verify(auditLogService).log(eq("PROFILE_SELECT"), eq("USER"), eq(user.getPublicId()), any(),
                eq(AuditLog.ActionStatus.FAILED));
    }

    @Test
    void missingOrBlankTokenIsRejected() {
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), null, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), "  ", Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void unknownTokenIsRejected() {
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), "unknown", Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void reusedRevokedTokenRevokesAllSessions() {
        stored.setRevoked(true);

        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), RAW_TOKEN, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(refreshTokenRepository).revokeAllByUserId(user.getPublicId());
        verifyNoInteractions(authTokenService);
    }

    @Test
    void expiredTokenIsRejected() {
        stored.setExpiresAt(LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), RAW_TOKEN, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void tokenOfAnotherUserIsRejectedAndLeftUntouched() {
        stored.setUserId("another-user");

        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), RAW_TOKEN, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(stored.isRevoked()).isFalse();
        verifyNoInteractions(authTokenService);
    }

    @Test
    void unknownOrInactiveUserIsRejected() {
        assertThatThrownBy(() -> useCase.execute("unknown-user", RAW_TOKEN, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);

        user.setActive(false);
        assertThatThrownBy(() -> useCase.execute(user.getPublicId(), RAW_TOKEN, Role.USER))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(stored.isRevoked()).isFalse();
        verifyNoInteractions(authTokenService);
    }

    @Test
    void heldRoleRevokesCurrentTokenAndIssuesSessionForThatProfileOnly() {
        user.getRoles().add(Role.ADMIN);
        when(authTokenService.startExclusiveSession(user, Role.USER)).thenReturn(new TokenPair("new-access", "new-refresh"));

        AuthSession session = useCase.execute(user.getPublicId(), RAW_TOKEN, Role.USER);

        assertThat(stored.isRevoked()).isTrue();
        assertThat(session.tokens().refreshToken()).isEqualTo("new-refresh");
        assertThat(session.user().activeRole()).isEqualTo(Role.USER);
        assertThat(session.user().permissions()).isEmpty(); // pas de cumul avec ADMIN
        verify(auditLogService).log("PROFILE_SELECT", "USER", user.getPublicId(), "profile=USER",
                AuditLog.ActionStatus.SUCCESS);
    }
}
