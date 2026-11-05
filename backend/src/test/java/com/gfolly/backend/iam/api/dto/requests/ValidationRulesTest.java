package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRulesTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Li", "Jean Paul", "  Li  ", "aaaaaaaaaaaaaaaaaaaaaaaaa"})
    void validNamesAreAccepted(String name) {
        assertThat(VALIDATOR.validate(update(name, "Doe"))).isEmpty();
        assertThat(VALIDATOR.validate(update("Jane", name))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A", "  A  ", "     ", "aaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void invalidNamesAreRejected(String name) {
        assertThat(VALIDATOR.validate(update(name, "Doe"))).isNotEmpty();
        assertThat(VALIDATOR.validate(update("Jane", name))).isNotEmpty();
    }

    @Test
    void nameRulesApplyToRegistrationAndCreation() {
        assertThat(VALIDATOR.validate(new RegisterRequest("A", "Doe", "jane@example.com", "Secret#123"))).isNotEmpty();
        assertThat(VALIDATOR.validate(new CreateUserRequest("Jane", "A", "jane@example.com", "Secret#123", Set.of(Role.USER))))
                .isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Aa1!aaaa", "Secret#123", "Aa1!aaaaaaaaaaaaaaaaaaaaa"})
    void validPasswordsAreAccepted(String password) {
        assertThat(VALIDATOR.validate(new RegisterRequest("Jane", "Doe", "jane@example.com", password))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Aa1!aaa",                    // 7 caractères
            "Aa1!aaaaaaaaaaaaaaaaaaaaaa", // 26 caractères
            "secret#123",                 // sans majuscule
            "SECRET#123",                 // sans minuscule
            "Secret#abc",                 // sans chiffre
            "Secret1234"                  // sans caractère spécial
    })
    void invalidPasswordsAreRejected(String password) {
        assertThat(VALIDATOR.validate(new RegisterRequest("Jane", "Doe", "jane@example.com", password))).isNotEmpty();
    }

    private static UpdateUserRequest update(String firstName, String lastName) {
        return new UpdateUserRequest(firstName, lastName, Set.of(Role.USER));
    }
}
