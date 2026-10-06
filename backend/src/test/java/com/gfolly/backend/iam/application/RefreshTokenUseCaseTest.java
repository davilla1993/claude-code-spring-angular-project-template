package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.application.dto.TokenPair;
import com.gfolly.backend.iam.domain.RefreshToken;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.OpaqueTokens;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefreshTokenUseCaseTest {

    private static final String RAW_TOKEN = "raw-refresh-token";

    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthTokenService authTokenService = mock(AuthTokenService.class);
    private final RefreshTokenUseCase useCase =
            new RefreshTokenUseCase(refreshTokenRepository, userRepository, authTokenService);

    private User user;
    private RefreshToken stored;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        stored = new RefreshToken();
        stored.setTokenHash(OpaqueTokens.hash(RAW_TOKEN));
        stored.setUserId(user.getPublicId());
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByTokenHash(OpaqueTokens.hash(RAW_TOKEN))).thenReturn(Optional.of(stored));
        when(userRepository.findByPublicIdAndDeletedFalse(user.getPublicId())).thenReturn(Optional.of(user));
    }

    @Test
    void validTokenIsRotated() {
        when(authTokenService.issueTokens(user)).thenReturn(new TokenPair("new-access", "new-refresh"));

        AuthSession session = useCase.execute(RAW_TOKEN);

        assertThat(stored.isRevoked()).isTrue();
        assertThat(session.tokens().refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void reusedRevokedTokenRevokesAllSessions() {
        stored.setRevoked(true);

        assertThatThrownBy(() -> useCase.execute(RAW_TOKEN)).isInstanceOf(InvalidCredentialsException.class);
        verify(refreshTokenRepository).revokeAllByUserId(user.getPublicId());
        verifyNoInteractions(authTokenService);
    }

    @Test
    void expiredTokenIsRejected() {
        stored.setExpiresAt(LocalDateTime.now().minusSeconds(1));

        assertThatThrownBy(() -> useCase.execute(RAW_TOKEN)).isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void unknownOrMissingTokenIsRejected() {
        assertThatThrownBy(() -> useCase.execute("unknown")).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> useCase.execute(null)).isInstanceOf(InvalidCredentialsException.class);
        verifyNoInteractions(authTokenService);
    }

    @Test
    void deactivatedUserCannotRefresh() {
        user.setActive(false);

        assertThatThrownBy(() -> useCase.execute(RAW_TOKEN)).isInstanceOf(InvalidCredentialsException.class);
        verify(authTokenService, never()).issueTokens(any());
    }
}
