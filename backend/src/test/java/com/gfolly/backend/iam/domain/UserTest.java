package com.gfolly.backend.iam.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void soleRoleIsEmptyWithoutAnyRole() {
        assertThat(new User().soleRole()).isEmpty();
    }

    @Test
    void soleRoleReturnsTheOnlyRole() {
        User user = new User();
        user.getRoles().add(Role.ADMIN);

        assertThat(user.soleRole()).contains(Role.ADMIN);
    }

    @Test
    void soleRoleIsEmptyWithSeveralRoles() {
        User user = new User();
        user.getRoles().add(Role.ADMIN);
        user.getRoles().add(Role.USER);

        assertThat(user.soleRole()).isEmpty();
    }
}
