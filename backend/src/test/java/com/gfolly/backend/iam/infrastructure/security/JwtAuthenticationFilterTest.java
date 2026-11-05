package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.shared.util.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);
    private final CurrentSessionChecker currentSessionChecker = mock(CurrentSessionChecker.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, currentSessionChecker);

    @BeforeEach
    void sessionIsCurrentByDefault() {
        when(currentSessionChecker.isCurrent(any(), any())).thenReturn(true);
    }

    @Test
    void tokenOfAClosedOrReplacedSessionLeavesRequestAnonymous() throws Exception {
        when(currentSessionChecker.isCurrent(any(), any())).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtService.generateAccessToken(user(Role.ADMIN), Role.ADMIN));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesFromCookieWithRoleAndPermissions() throws Exception {
        User admin = user(Role.ADMIN);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieService.ACCESS_TOKEN_COOKIE, jwtService.generateAccessToken(admin, Role.ADMIN)));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(((UserPrincipal) auth.getPrincipal()).userId()).isEqualTo(admin.getPublicId());
        assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "user:create", "audit:log:view");
    }

    @Test
    void authenticatesFromBearerHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtService.generateAccessToken(user(Role.USER), Role.USER));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void invalidTokenLeavesRequestAnonymous() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer forged.token.value");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull(); // la requête continue (401 décidé par Spring Security)
    }

    @Test
    void tokenWithoutActiveProfileAuthenticatesWithoutAnyAuthority() throws Exception {
        User user = user(Role.ADMIN);
        user.getRoles().add(Role.USER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtService.generateAccessToken(user, null));
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.isAuthenticated()).isTrue();
        assertThat(auth.getAuthorities()).isEmpty();
        assertThat(((UserPrincipal) auth.getPrincipal()).activeRole()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void tokenWithUnknownRoleClaimLeavesRequestAnonymous() throws Exception {
        String token = Jwts.builder()
                .subject("some-user")
                .claim(JwtService.CLAIM_ROLE, "SUPERADMIN")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(chain.getRequest()).isNotNull();
    }

    private static User user(Role role) {
        User user = new User();
        user.setEmail("someone@example.com");
        user.setFirstName("Some");
        user.setLastName("One");
        user.getRoles().add(role);
        return user;
    }
}
