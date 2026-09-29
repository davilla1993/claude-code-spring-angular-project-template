package com.gfolly.quantly_backend.iam.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfolly.quantly_backend.shared.api.ApiResponse;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Un Commander en impersonation (token JWT portant le claim "imp_src") ne peut
 * effectuer aucune écriture sur les données du tenant impersonné : lecture seule.
 * Les modifications restent la responsabilité du propriétaire de la boutique ou
 * de ses collaborateurs.
 *
 * Deux endpoints de contrôle de session restent autorisés en écriture pour ne
 * pas piéger le Commander dans le mode support.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImpersonationWriteGuardFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private static final Set<String> ALLOWED_PATHS_DURING_IMPERSONATION = Set.of(
            "/api/superadmin/stop-impersonation",
            "/api/auth/logout"
    );

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SAFE_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (ALLOWED_PATHS_DURING_IMPERSONATION.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractFromCookie(request);
        if (token == null) {
            token = extractFromHeader(request);
        }

        if (token != null && jwtService.isTokenValid(token)) {
            try {
                String impersonatorId = jwtService.extractImpersonatorId(token);
                if (impersonatorId != null) {
                    log.warn("[IMPERSONATION] Blocked write attempt by commander {} on {} {}",
                            impersonatorId, request.getMethod(), request.getRequestURI());
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    objectMapper.writeValue(response.getWriter(),
                            ApiResponse.error(ErrorMessages.IMPERSONATION_READ_ONLY));
                    return;
                }
            } catch (Exception e) {
                log.warn("Impersonation guard failed to inspect token: {}", e.getMessage());
            }
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
