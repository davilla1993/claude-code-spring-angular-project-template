package com.gfolly.quantly_backend.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.gfolly.quantly_backend.shared.api.ApiResponse;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limits sensitive authentication attempts to 5 per account (email or userId) per 15 minutes.
 * Keying on the account identifier (not IP) ensures that only the targeted account
 * is affected, avoiding network-wide blocks.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final List<String> SENSITIVE_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification"
    );

    private final Cache<String, AtomicInteger> cache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !("POST".equalsIgnoreCase(request.getMethod()) && SENSITIVE_PATHS.contains(path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request);

        String identifier = extractIdentifier(wrappedRequest);
        
        // If no identifier found (invalid body), we still allow the request to proceed
        // and let the controller/validator handle it.
        if (identifier != null && !identifier.isBlank()) {
            String key = identifier.toLowerCase();
            AtomicInteger counter = cache.get(key, k -> new AtomicInteger(0));

            if (counter.incrementAndGet() > MAX_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                objectMapper.writeValue(response.getWriter(),
                        ApiResponse.error(ErrorMessages.RATE_LIMIT_EXCEEDED));
                return;
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private String extractIdentifier(CachedBodyRequestWrapper request) {
        try {
            JsonNode node = objectMapper.readTree(request.getInputStream());
            
            // Try 'email' first, then 'userId'
            JsonNode emailNode = node.get("email");
            if (emailNode != null && !emailNode.isNull()) {
                return emailNode.asText();
            }
            
            JsonNode userNode = node.get("userId");
            if (userNode != null && !userNode.isNull()) {
                return userNode.asText();
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
