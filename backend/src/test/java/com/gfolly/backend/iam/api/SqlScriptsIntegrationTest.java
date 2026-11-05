package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exécute les scripts de docs/scripts sur le schéma réel, puis vérifie la connexion au compte obtenu. Ignoré sans Docker. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SqlScriptsIntegrationTest {

    private static final String SCRIPTS_DIR = "../docs/scripts/";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void createAdminGivesTheAdminRoleToARegisteredUser() throws Exception {
        String password = "Secret#1234";
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmailVerified(true);
        user.getRoles().add(Role.USER);
        userRepository.saveAndFlush(user);

        run("create-admin.sql");

        login("admin@example.com", password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles", contains("ADMIN")))
                .andExpect(jsonPath("$.data.activeRole").value("ADMIN"));
    }

    @Test
    void createUserTwoRolesCreatesAUserWhoMustChooseAProfile() throws Exception {
        run("create-user-two-roles.sql");

        login("deux.roles@example.com", "A-Remplacer#2026")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roles", contains("ADMIN", "USER")))
                .andExpect(jsonPath("$.data.activeRole").value(nullValue()))
                .andExpect(jsonPath("$.data.firstLogin").value(true));
    }

    private void run(String script) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new FileSystemResource(SCRIPTS_DIR + script));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
    }
}
