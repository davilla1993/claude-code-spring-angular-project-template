package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestsValidationTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    static Stream<Arguments> invalidRoles() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(Set.of()),
                Arguments.of(Collections.singleton(null)));
    }

    @ParameterizedTest
    @MethodSource("invalidRoles")
    void createRequestRejectsInvalidRoles(Set<Role> roles) {
        assertOnlyRolesRejected(VALIDATOR.validate(createRequest(roles)));
    }

    @ParameterizedTest
    @MethodSource("invalidRoles")
    void updateRequestRejectsInvalidRoles(Set<Role> roles) {
        assertOnlyRolesRejected(VALIDATOR.validate(new UpdateUserRequest("Jane", "Doe", roles)));
    }

    @Test
    void severalRolesAreAccepted() {
        Set<Role> roles = Set.of(Role.ADMIN, Role.USER);

        assertThat(VALIDATOR.validate(createRequest(roles))).isEmpty();
        assertThat(VALIDATOR.validate(new UpdateUserRequest("Jane", "Doe", roles))).isEmpty();
    }

    private static CreateUserRequest createRequest(Set<Role> roles) {
        return new CreateUserRequest("Jane", "Doe", "jane@example.com", "Temp1234!", roles);
    }

    private static <T> void assertOnlyRolesRejected(Set<ConstraintViolation<T>> violations) {
        assertThat(violations).isNotEmpty();
        assertThat(violations).allSatisfy(violation ->
                assertThat(violation.getPropertyPath().toString()).startsWith("roles"));
    }
}
