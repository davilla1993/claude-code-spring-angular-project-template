package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lecture des fichiers par l'API, avec la vraie chaîne de sécurité. Ignoré sans Docker. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UploadsIntegrationTest {

    private static final String PASSWORD = "Secret#1234";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;
    @Autowired
    private FileStorageService fileStorage;
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
    void anonymousRequestIsRefused() throws Exception {
        String key = storePng();

        mockMvc.perform(get("/api/uploads/" + key)).andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserReadsAFileWithAServerSideContentType() throws Exception {
        String key = storePng();

        mockMvc.perform(get("/api/uploads/" + key).cookie(login()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().longValue("Content-Length", 3))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void unknownOrInvalidKeyReturnsNotFound() throws Exception {
        Cookie[] session = login();

        mockMvc.perform(get("/api/uploads/" + UUID.randomUUID() + ".png").cookie(session))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/uploads/secret.png").cookie(session))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/uploads/" + UUID.randomUUID() + ".html").cookie(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void sessionWithoutActiveProfileIsRefused() throws Exception {
        String key = storePng();

        mockMvc.perform(get("/api/uploads/" + key).cookie(login(Role.ADMIN, Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void forgedTokenIsRefused() throws Exception {
        String key = storePng();

        mockMvc.perform(get("/api/uploads/" + key).cookie(new Cookie("access_token", "forged.token.value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void traversalAttemptsAreRefusedEvenWithASession() throws Exception {
        Cookie[] session = login();

        for (String path : List.of(
                "/api/uploads/..%2f..%2fapplication.yml",
                "/api/uploads/%2e%2e%2fapplication.yml",
                "/api/uploads/..%5capplication.yml",
                "/api/uploads/..;/application.yml",
                "/api/uploads/%00.png",
                "/api/uploads/sub/secret.png")) {
            mockMvc.perform(get(URI.create(path)).cookie(session)).andExpect(status().is4xxClientError());
        }
    }

    @Test
    void theFileCannotBeModifiedThroughTheApi() throws Exception {
        String key = storePng();
        Cookie[] session = login();

        mockMvc.perform(delete("/api/uploads/" + key).cookie(session));
        mockMvc.perform(put("/api/uploads/" + key).cookie(session));
        mockMvc.perform(post("/api/uploads/" + key).cookie(session));

        mockMvc.perform(get("/api/uploads/" + key).cookie(session))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void writeMethodsReturnMethodNotAllowed() throws Exception {
        String key = storePng();
        Cookie[] session = login();

        mockMvc.perform(delete("/api/uploads/" + key).cookie(session))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void theRealConfigurationRefusesActiveContent() {
        for (String name : List.of("x.html", "x.htm", "x.xhtml", "x.svg", "x.js", "x.xml")) {
            MockMultipartFile file = new MockMultipartFile("file", name, "image/png", new byte[]{1});

            assertThatThrownBy(() -> fileStorage.store(file)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private String storePng() {
        return fileStorage.store(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3}));
    }

    private Cookie[] login() throws Exception {
        return login(Role.USER);
    }

    private Cookie[] login(Role... roles) throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmailVerified(true);
        user.getRoles().addAll(List.of(roles));
        userRepository.saveAndFlush(user);

        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookies();
    }
}
