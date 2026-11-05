package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Mapping de user_roles et filtre par rôle sur un vrai PostgreSQL. Ignoré sans Docker. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private UserRepository userRepository;

    @Test
    void filtersByHeldRoleWithPaginationAndWithoutDuplicates() {
        save("admin@example.com", Role.ADMIN);
        save("user@example.com", Role.USER);
        save("both@example.com", Role.ADMIN, Role.USER);
        User deleted = save("deleted@example.com", Role.ADMIN);
        deleted.setDeleted(true);
        userRepository.flush();

        var firstPage = userRepository.findAllNotDeletedHavingRole(
                Role.ADMIN, PageRequest.of(0, 1, Sort.by("email")));
        var all = userRepository.findAllNotDeletedHavingRole(
                Role.ADMIN, PageRequest.of(0, 10, Sort.by("email")));

        assertThat(firstPage.getTotalElements()).isEqualTo(2);
        assertThat(firstPage.getContent()).hasSize(1);
        assertThat(all.getContent()).extracting(User::getEmail)
                .containsExactly("admin@example.com", "both@example.com");
    }

    private User save(String email, Role... roles) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setFirstName("First");
        user.setLastName("Last");
        user.getRoles().addAll(List.of(roles));
        return userRepository.saveAndFlush(user);
    }
}
