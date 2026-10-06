package com.gfolly.backend.iam.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Gestion des cookies d'authentification (HttpOnly, SameSite=Strict).
 * <ul>
 *   <li>access_token : envoyé sur tout /api</li>
 *   <li>refresh_token : envoyé uniquement sur /api/auth (refresh et logout)</li>
 * </ul>
 */
@Service
public class CookieService {

    public static final String ACCESS_TOKEN_COOKIE  = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final String ACCESS_TOKEN_PATH  = "/api";
    private static final String REFRESH_TOKEN_PATH = "/api/auth";

    private final boolean secure;
    private final Duration accessTokenMaxAge;
    private final Duration refreshTokenMaxAge;

    public CookieService(
            @Value("${app.security.cookie-secure}") boolean secure,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpirationMs) {
        this.secure = secure;
        this.accessTokenMaxAge = Duration.ofMillis(accessTokenExpirationMs);
        this.refreshTokenMaxAge = Duration.ofMillis(refreshTokenExpirationMs);
    }

    public void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, ACCESS_TOKEN_PATH, accessTokenMaxAge);
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, REFRESH_TOKEN_PATH, refreshTokenMaxAge);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", ACCESS_TOKEN_PATH, Duration.ZERO);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH, Duration.ZERO);
    }

    public static Optional<String> readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(value -> !value.isBlank())
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
