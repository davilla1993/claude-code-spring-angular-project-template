package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.UserPrincipal;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUserUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final GetUserUseCase useCase = new GetUserUseCase(userRepository);

    @Test
    void heldActiveProfileIsExposedWithItsPermissions() {
        User user = user(Role.ADMIN, Role.USER);

        var response = useCase.executeForSession(principal(user, "ADMIN"));

        assertThat(response.activeRole()).isEqualTo(Role.ADMIN);
        assertThat(response.permissions()).contains("user:view");
    }

    @Test
    void withdrawnRoleIsNoLongerPresentedAsActive() {
        User user = user(Role.USER);

        var response = useCase.executeForSession(principal(user, "ADMIN"));

        assertThat(response.activeRole()).isNull();
        assertThat(response.permissions()).isEmpty();
    }

    @Test
    void sessionWithoutProfileHasNoActiveRole() {
        User user = user(Role.ADMIN, Role.USER);

        var response = useCase.executeForSession(principal(user, null));

        assertThat(response.activeRole()).isNull();
        assertThat(response.permissions()).isEmpty();
    }

    private UserPrincipal principal(User user, String activeRole) {
        return new UserPrincipal(user.getPublicId(), user.getEmail(), user.getFullName(), activeRole);
    }

    private User user(Role... roles) {
        User user = new User();
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.getRoles().addAll(java.util.List.of(roles));
        when(userRepository.findByPublicIdAndDeletedFalse(user.getPublicId())).thenReturn(Optional.of(user));
        return user;
    }
}
