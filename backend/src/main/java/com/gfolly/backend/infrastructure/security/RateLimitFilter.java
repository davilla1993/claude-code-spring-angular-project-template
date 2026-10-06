package com.gfolly.backend.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite les tentatives sur les endpoints d'authentification sensibles à {@value #MAX_ATTEMPTS}
 * par compte (email ou userId du corps de requête) et par endpoint, sur une fenêtre de 15 minutes.
 * <p>
 * La clé est le compte ciblé (et non l'IP) : seul le compte attaqué est protégé/bloqué,
 * sans pénaliser tout un réseau. Compteurs en mémoire : à remplacer par un store partagé
 * (Redis, Bucket4j...) si l'application tourne sur plusieurs instances.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private static final Set<String> SENSITIVE_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification"
    );

    private final Cache<String, AtomicInteger> attempts = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW)
            .maximumSize(100_000)
            .build();

    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && SENSITIVE_PATHS.contains(pathOf(request)));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request);
        String identifier = extractIdentifier(wrappedRequest);

        // Corps invalide ou sans identifiant : la validation du controller renverra l'erreur adaptée.
        if (identifier != null) {
            String key = pathOf(request) + ":" + identifier.trim().toLowerCase(Locale.ROOT);
            AtomicInteger counter = attempts.get(key, k -> new AtomicInteger());
            if (counter.incrementAndGet() > MAX_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(), ApiResponse.error(ErrorMessages.RATE_LIMIT_EXCEEDED));
                return;
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private String extractIdentifier(CachedBodyRequestWrapper request) {
        try {
            JsonNode node = objectMapper.readTree(request.getInputStream());
            for (String field : new String[]{"email", "userId"}) {
                JsonNode value = node.get(field);
                if (value != null && value.isString() && !value.asString().isBlank()) {
                    return value.asString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String pathOf(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
