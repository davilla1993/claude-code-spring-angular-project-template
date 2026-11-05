package com.gfolly.backend.iam.infrastructure.mapper.request;

import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.requests.RegisterRequest;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestMapperTest {

    private static final String HASH = "hash";

    @Test
    void adminCreationKeepsAllRequestedRoles() {
        var request = new CreateUserRequest("Jane", "Doe", "jane@example.com", "Temp1234!",
                Set.of(Role.ADMIN, Role.USER));

        User user = UserRequestMapper.toNewUser(request, HASH);

        assertThat(user.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(user.isFirstLogin()).isTrue();
    }

    @Test
    void publicRegistrationGrantsUserRoleOnly() {
        var request = new RegisterRequest("Jane", "Doe", "jane@example.com", "Passw0rd!");

        User user = UserRequestMapper.toNewUser(request, HASH);

        assertThat(user.getRoles()).containsExactly(Role.USER);
        assertThat(user.isFirstLogin()).isFalse();
    }
}
