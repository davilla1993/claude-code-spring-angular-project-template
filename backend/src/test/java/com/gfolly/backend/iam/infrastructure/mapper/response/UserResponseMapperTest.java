package com.gfolly.backend.iam.infrastructure.mapper.response;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseMapperTest {

    @Test
    void rolesAreSortedWhateverTheStoredOrder() {
        // Ordre d'insertion volontairement inverse de l'ordre de tri.
        UserResponse response = UserResponseMapper.toResponse(user(Role.USER, Role.ADMIN));

        assertThat(response.roles()).containsExactly(Role.ADMIN, Role.USER);
    }

    @Test
    void outOfSessionResponseHasNeitherActiveRoleNorPermissions() {
        UserResponse response = UserResponseMapper.toResponse(user(Role.ADMIN));

        assertThat(response.roles()).containsExactly(Role.ADMIN);
        assertThat(response.activeRole()).isNull();
        assertThat(response.permissions()).isEmpty();
    }

    @Test
    void sessionResponseExposesPermissionsOfTheActiveProfileOnly() {
        User user = user(Role.ADMIN, Role.USER);

        UserResponse asAdmin = UserResponseMapper.toResponse(user, Role.ADMIN);
        UserResponse asUser = UserResponseMapper.toResponse(user, Role.USER);

        assertThat(asAdmin.activeRole()).isEqualTo(Role.ADMIN);
        assertThat(asAdmin.permissions()).containsExactly("audit:log:view", "user:create", "user:update", "user:view");
        assertThat(asUser.activeRole()).isEqualTo(Role.USER);
        assertThat(asUser.permissions()).isEmpty();
    }

    @Test
    void permissionsAreEmptyWithSeveralRoles() {
        UserResponse response = UserResponseMapper.toResponse(user(Role.ADMIN, Role.USER));

        assertThat(response.permissions()).isEmpty();
    }

    private static User user(Role... roles) {
        User user = new User();
        user.setEmail("someone@example.com");
        user.setFirstName("Some");
        user.setLastName("One");
        user.setRoles(new LinkedHashSet<>(List.of(roles)));
        return user;
    }
}
