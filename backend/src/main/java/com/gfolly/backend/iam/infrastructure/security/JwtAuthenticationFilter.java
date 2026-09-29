package com.gfolly.quantly_backend.iam.infrastructure.security;

import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.shared.util.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Priorité 1 : HttpOnly cookie (navigateur Angular)
        // Priorité 2 : Authorization: Bearer (Postman / API clients)
        String token = extractFromCookie(request);
        if (token == null) {
            token = extractFromHeader(request);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            String fullName = jwtService.extractFullName(token);
            String tenantId = jwtService.extractTenantId(token);
            String tenantSlug = jwtService.extractTenantSlug(token);
            String role = jwtService.extractRole(token).name();
            List<String> permissions = jwtService.extractPermissions(token);

            TenantContext.setCurrentTenant(tenantId);

            // Combinaison du rôle et des permissions
            List<SimpleGrantedAuthority> authorities = permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            UserPrincipal principal = new UserPrincipal(userId, email, fullName, tenantSlug);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authenticated — userId: {}, tenantId: {}, role: {}, permissions count: {}", 
                    userId, tenantId, role, permissions.size());
        } catch (Exception e) {
            log.warn("JWT processing failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> CookieService.ACCESS_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }
}

