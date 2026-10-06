package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.shared.util.UserPrincipal;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService =
            new JwtService("test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz", 60_000);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesFromCookieWithRoleAndPermissions() throws Exception {
        User admin = user(Role.ADMIN);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(CookieService.ACCESS_TOKEN_COOKIE, jwtService.generateAccessToken(admin)));

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
        request.addHeader("Authorization", "Bearer " + jwtService.generateAccessToken(user(Role.USER)));

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

    private static User user(Role role) {
        User user = new User();
        user.setEmail("someone@example.com");
        user.setFirstName("Some");
        user.setLastName("One");
        user.setRole(role);
        return user;
    }
}
