package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.UpdateUserRequest;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Règles anti auto-sabotage : un utilisateur ne peut ni changer son propre rôle, ni se désactiver.
 */
class UserAdministrationUseCasesTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final AuditLogService auditLogService = mock(AuditLogService.class);

    private final UpdateUserUseCase updateUser =
            new UpdateUserUseCase(userRepository, refreshTokenRepository, auditLogService);
    private final SetUserActiveUseCase setUserActive =
            new SetUserActiveUseCase(userRepository, refreshTokenRepository, auditLogService);

    private User admin;
    private User other;

    @BeforeEach
    void setUp() {
        admin = user(Role.ADMIN);
        other = user(Role.USER);
        when(userRepository.findByPublicIdAndDeletedFalse(admin.getPublicId())).thenReturn(Optional.of(admin));
        when(userRepository.findByPublicIdAndDeletedFalse(other.getPublicId())).thenReturn(Optional.of(other));
    }

    @Test
    void adminCannotChangeOwnRole() {
        String id = admin.getPublicId();

        assertThatThrownBy(() -> updateUser.execute(id, id, new UpdateUserRequest("A", "B", Set.of(Role.USER))))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(admin.getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    void adminCanEditOwnNameWithoutChangingRole() {
        String id = admin.getPublicId();

        var response = updateUser.execute(id, id, new UpdateUserRequest(" New ", " Name ", Set.of(Role.ADMIN)));

        assertThat(response.firstName()).isEqualTo("New");
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyString());
    }

    @Test
    void roleChangeOnAnotherUserRevokesTheirSessions() {
        updateUser.execute(admin.getPublicId(), other.getPublicId(), new UpdateUserRequest("A", "B", Set.of(Role.ADMIN)));

        assertThat(other.getRoles()).containsExactly(Role.ADMIN);
        verify(refreshTokenRepository).revokeAllByUserId(other.getPublicId());
    }

    @Test
    void addingSecondRoleToAnotherUserReplacesRolesAndRevokesSessions() {
        var response = updateUser.execute(admin.getPublicId(), other.getPublicId(),
                new UpdateUserRequest("A", "B", Set.of(Role.USER, Role.ADMIN)));

        assertThat(other.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(response.roles()).containsExactly(Role.ADMIN, Role.USER);
        assertThat(response.permissions()).isEmpty();
        assertThat(other.getUpdatedAt()).isNotNull();
        verify(refreshTokenRepository).revokeAllByUserId(other.getPublicId());
    }

    @Test
    void sameRoleSetDoesNotRevokeSessions() {
        other.getRoles().add(Role.ADMIN);

        updateUser.execute(admin.getPublicId(), other.getPublicId(),
                new UpdateUserRequest("A", "B", Set.of(Role.ADMIN, Role.USER)));

        assertThat(other.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(other.getFirstName()).isEqualTo("A");
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyString());
    }

    @Test
    void adminCannotAddRoleToThemselves() {
        String id = admin.getPublicId();
        var request = new UpdateUserRequest("A", "B", Set.of(Role.ADMIN, Role.USER));

        assertThatThrownBy(() -> updateUser.execute(id, id, request))
                .isInstanceOf(ForbiddenOperationException.class);

        assertThat(admin.getRoles()).containsExactly(Role.ADMIN);
        assertThat(admin.getFirstName()).isEqualTo("First");
        verify(refreshTokenRepository, never()).revokeAllByUserId(anyString());
    }

    @Test
    void userCannotDeactivateThemselves() {
        String id = admin.getPublicId();

        assertThatThrownBy(() -> setUserActive.execute(id, id, false))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    void deactivationRevokesAllSessions() {
        setUserActive.execute(admin.getPublicId(), other.getPublicId(), false);

        assertThat(other.isActive()).isFalse();
        verify(refreshTokenRepository).revokeAllByUserId(other.getPublicId());
    }

    private static User user(Role role) {
        User user = new User();
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.getRoles().add(role);
        return user;
    }
}
