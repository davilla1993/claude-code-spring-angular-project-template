package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.iam.infrastructure.security.CookieService;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.repository.AuditLogRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Profil actif de bout en bout : vraie chaîne Spring Security, vrai PostgreSQL. Ignoré sans Docker.
 * Pas de @Transactional : l'audit et la révocation des tokens doivent être réellement validés.
 * Chaque test utilise son propre compte (limite de 5 connexions par compte, réutilisation de token = sessions révoquées).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ActiveProfileIntegrationTest {

    private static final String PASSWORD = "Secret#123";
    private static final String ACTIVE_PROFILE_URL = "/api/auth/active-profile";
    private static final String ME_URL = "/api/users/me";
    private static final String USERS_URL = "/api/users";
    private static final String REFRESH_URL = "/api/auth/refresh";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine");

    @Autowired
    private WebApplicationContext context;
    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter springSecurityFilterChain;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void userWithTwoRolesStartsWithoutProfileThenSwitchesWithoutCumulatingPermissions() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);

        // Connexion : aucun profil, aucune permission.
        ResultActions login = login(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value(nullValue()))
                .andExpect(jsonPath("$.data.permissions", empty()));
        Session noProfile = sessionOf(login);

        mockMvc.perform(get(ME_URL).cookie(noProfile.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value(nullValue()))
                .andExpect(jsonPath("$.data.permissions", empty()));
        mockMvc.perform(get(USERS_URL).cookie(noProfile.access())).andExpect(status().isForbidden());

        // Choix de USER : toujours pas d'accès à l'administration, bien que l'utilisateur détienne ADMIN.
        ResultActions selectUser = select(noProfile, "USER").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value("USER"))
                .andExpect(jsonPath("$.data.permissions", empty()));
        Session asUser = sessionOf(selectUser);
        mockMvc.perform(get(USERS_URL).cookie(asUser.access())).andExpect(status().isForbidden());

        // Passage à ADMIN : accès accordé, permissions de ce seul profil.
        ResultActions selectAdmin = select(asUser, "ADMIN").andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions", hasItem("user:view")));
        Session asAdmin = sessionOf(selectAdmin);
        mockMvc.perform(get(USERS_URL).cookie(asAdmin.access())).andExpect(status().isOk());
        mockMvc.perform(get(ME_URL).cookie(asAdmin.access()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value("ADMIN"));

        // Audit : profil de l'auteur au moment de l'action, c'est-à-dire avant le changement.
        List<AuditLog> entries = profileSelections(user);
        assertThat(entries).extracting(AuditLog::getDetails).containsExactly("profile=USER", "profile=ADMIN");
        assertThat(entries).extracting(AuditLog::getActiveProfile).containsExactly(null, "USER");
        assertThat(entries).extracting(AuditLog::getUserEmail).containsOnly(user.getEmail());
        assertThat(entries).extracting(AuditLog::getStatus).containsOnly(AuditLog.ActionStatus.SUCCESS);
    }

    @Test
    void userWithSingleRoleHasProfileActivatedAndCannotSelectARoleNotHeld() throws Exception {
        User user = saveUser(Role.USER);

        Session session = sessionOf(login(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value("USER")));

        select(session, "ADMIN").andExpect(status().isForbidden());

        mockMvc.perform(get(USERS_URL).cookie(session.access())).andExpect(status().isForbidden());
        assertThat(profileSelections(user)).singleElement().satisfies(entry -> {
            assertThat(entry.getStatus()).isEqualTo(AuditLog.ActionStatus.FAILED);
            assertThat(entry.getActiveProfile()).isEqualTo("USER");
        });
        // Le refus n'a pas consommé la session.
        mockMvc.perform(post(REFRESH_URL).cookie(session.refresh())).andExpect(status().isOk());
    }

    @Test
    void refreshKeepsTheActiveProfile() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session asAdmin = sessionOf(select(sessionOf(login(user)), "ADMIN").andExpect(status().isOk()));

        Session refreshed = sessionOf(mockMvc.perform(post(REFRESH_URL).cookie(asAdmin.refresh()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value("ADMIN"))
                .andExpect(jsonPath("$.data.permissions", hasItem("user:view"))));

        mockMvc.perform(get(USERS_URL).cookie(refreshed.access())).andExpect(status().isOk());
    }

    @Test
    void refreshWithoutProfileGrantsNoProfile() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session noProfile = sessionOf(login(user));

        Session refreshed = sessionOf(mockMvc.perform(post(REFRESH_URL).cookie(noProfile.refresh()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeRole").value(nullValue()))
                .andExpect(jsonPath("$.data.permissions", empty())));

        mockMvc.perform(get(USERS_URL).cookie(refreshed.access())).andExpect(status().isForbidden());
    }

    @Test
    void previousRefreshTokenIsRejectedAfterSwitching() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session noProfile = sessionOf(login(user));
        Session asAdmin = sessionOf(select(noProfile, "ADMIN").andExpect(status().isOk()));

        mockMvc.perform(post(REFRESH_URL).cookie(noProfile.refresh())).andExpect(status().isUnauthorized());
        // Pour le changement de profil aussi : l'ancien token ne vaut plus rien.
        select(new Session(asAdmin.access(), noProfile.refresh()), "USER").andExpect(status().isUnauthorized());
    }

    @Test
    void selectingAProfileRequiresARefreshTokenOfTheSameUser() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session session = sessionOf(login(user));
        Session other = sessionOf(login(saveUser(Role.ADMIN, Role.USER)));

        mockMvc.perform(put(ACTIVE_PROFILE_URL).cookie(session.access()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isUnauthorized());
        select(new Session(session.access(), other.refresh()), "USER").andExpect(status().isUnauthorized());

        mockMvc.perform(put(ACTIVE_PROFILE_URL).cookie(session.refresh()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isUnauthorized()); // sans access token
    }

    @Test
    void secondLoginClosesTheFirstSessionWithoutHarmingTheNewOne() throws Exception {
        User user = saveUser(Role.ADMIN);
        Session first = sessionOf(login(user).andExpect(status().isOk()));
        mockMvc.perform(get(USERS_URL).cookie(first.access())).andExpect(status().isOk());

        Session second = sessionOf(login(user).andExpect(status().isOk()));

        mockMvc.perform(get(USERS_URL).cookie(first.access())).andExpect(status().isUnauthorized());
        // L'ancien appareil tente un refresh : refus simple, sans déclencher la détection de vol.
        mockMvc.perform(post(REFRESH_URL).cookie(first.refresh())).andExpect(status().isUnauthorized());

        mockMvc.perform(get(USERS_URL).cookie(second.access())).andExpect(status().isOk());
        mockMvc.perform(post(REFRESH_URL).cookie(second.refresh())).andExpect(status().isOk());
    }

    @Test
    void changingProfileInvalidatesThePreviousAccessTokenImmediately() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session asAdmin = sessionOf(select(sessionOf(login(user)), "ADMIN").andExpect(status().isOk()));

        Session asUser = sessionOf(select(asAdmin, "USER").andExpect(status().isOk()));

        mockMvc.perform(get(USERS_URL).cookie(asAdmin.access())).andExpect(status().isUnauthorized());
        mockMvc.perform(get(USERS_URL).cookie(asUser.access())).andExpect(status().isForbidden());
    }

    @Test
    void logoutClosesTheSessionImmediately() throws Exception {
        Session session = sessionOf(login(saveUser(Role.ADMIN)));

        mockMvc.perform(post("/api/auth/logout").cookie(session.refresh())).andExpect(status().isOk());

        mockMvc.perform(get(USERS_URL).cookie(session.access())).andExpect(status().isUnauthorized());
    }

    @Test
    void roleChangeClosesTheSessionImmediately() throws Exception {
        User target = saveUser(Role.ADMIN);
        Session targetSession = sessionOf(login(target));
        Session admin = sessionOf(login(saveUser(Role.ADMIN)));

        mockMvc.perform(put(USERS_URL + "/" + target.getPublicId()).cookie(admin.access())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"First\",\"lastName\":\"Last\",\"roles\":[\"USER\"]}"))
                .andExpect(status().isOk());

        mockMvc.perform(get(USERS_URL).cookie(targetSession.access())).andExpect(status().isUnauthorized());
    }

    /**
     * Un navigateur n'envoie un cookie que si le chemin de la requête correspond à son attribut Path :
     * ce test ne transmet que les cookies qu'un navigateur transmettrait réellement.
     */
    @Test
    void profileCanBeSelectedWithTheCookiesABrowserActuallySends() throws Exception {
        User user = saveUser(Role.ADMIN, Role.USER);
        Session session = sessionOf(login(user));

        mockMvc.perform(put(ACTIVE_PROFILE_URL)
                        .cookie(sentByBrowserTo(ACTIVE_PROFILE_URL, session.access(), session.refresh()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    private static Cookie[] sentByBrowserTo(String url, Cookie... cookies) {
        return Arrays.stream(cookies)
                .filter(cookie -> url.equals(cookie.getPath()) || url.startsWith(cookie.getPath() + "/"))
                .toArray(Cookie[]::new);
    }

    private ResultActions login(User user) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(user.getEmail(), PASSWORD)));
    }

    private ResultActions select(Session session, String role) throws Exception {
        return mockMvc.perform(put(ACTIVE_PROFILE_URL).cookie(session.access(), session.refresh())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"%s\"}".formatted(role)));
    }

    private static Session sessionOf(ResultActions result) {
        var response = result.andReturn().getResponse();
        Cookie access = response.getCookie(CookieService.ACCESS_TOKEN_COOKIE);
        Cookie refresh = response.getCookie(CookieService.REFRESH_TOKEN_COOKIE);
        assertThat(access).as("cookie access_token").isNotNull();
        assertThat(refresh).as("cookie refresh_token").isNotNull();
        return new Session(access, refresh);
    }

    private List<AuditLog> profileSelections(User user) {
        return auditLogRepository.findAll().stream()
                .filter(entry -> "PROFILE_SELECT".equals(entry.getAction()))
                .filter(entry -> user.getPublicId().equals(entry.getEntityId()))
                .sorted(Comparator.comparing(AuditLog::getId))
                .toList();
    }

    private User saveUser(Role... roles) {
        User user = new User();
        user.setEmail("user-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEmailVerified(true);
        user.getRoles().addAll(List.of(roles));
        return userRepository.saveAndFlush(user);
    }

    private record Session(Cookie access, Cookie refresh) {}
}
