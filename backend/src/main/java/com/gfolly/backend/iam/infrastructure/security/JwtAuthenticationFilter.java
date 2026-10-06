package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.Permission;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.shared.util.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Authentifie la requête à partir du JWT.
 * Source 1 : cookie HttpOnly (navigateur). Source 2 : en-tête Authorization: Bearer (clients API).
 * Non déclaré en @Component : instancié par SecurityConfig pour n'être exécuté que dans la chaîne de sécurité.
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        resolveToken(request)
                .flatMap(jwtService::parseAccessToken)
                .ifPresent(this::authenticate);

        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        return CookieService.readCookie(request, CookieService.ACCESS_TOKEN_COOKIE)
                .or(() -> Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                        .filter(header -> header.startsWith(BEARER_PREFIX))
                        .map(header -> header.substring(BEARER_PREFIX.length())));
    }

    private void authenticate(Claims claims) {
        Role role;
        try {
            role = Role.valueOf(claims.get(JwtService.CLAIM_ROLE, String.class));
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("JWT with unknown role for user {}", claims.getSubject());
            return;
        }

        UserPrincipal principal = new UserPrincipal(
                claims.getSubject(),
                claims.get(JwtService.CLAIM_EMAIL, String.class),
                claims.get(JwtService.CLAIM_FULL_NAME, String.class));

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authoritiesOf(role));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static List<GrantedAuthority> authoritiesOf(Role role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getCode()));
        }
        return authorities;
    }
}
