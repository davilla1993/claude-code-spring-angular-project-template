package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ListUsersUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ListUsersUseCase listUsers = new ListUsersUseCase(userRepository);

    @ParameterizedTest
    @ValueSource(strings = {"roles", "passwordHash"})
    void sortOnUnauthorizedPropertyIsRejected(String property) {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(property));

        assertThatThrownBy(() -> listUsers.execute(null, pageable))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(userRepository);
    }

    @Test
    void sortOnAllowedPropertyIsAccepted() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt"));
        when(userRepository.findAllByDeletedFalse(any(Pageable.class))).thenReturn(Page.empty(pageable));

        assertThat(listUsers.execute(null, pageable)).isNotNull();
    }
}
