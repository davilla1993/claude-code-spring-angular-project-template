package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Émission et validation des access tokens JWT (HS256).
 * Les permissions ne sont pas embarquées : elles sont dérivées du rôle à chaque requête.
 */
@Service
@Slf4j
public class JwtService {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpirationMs) {
        // Lève WeakKeyException si le secret fait moins de 256 bits : échec au démarrage plutôt qu'en production.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getPublicId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_FULL_NAME, user.getFullName())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Vérifie la signature et l'expiration du token.
     *
     * @return les claims si le token est valide, vide sinon
     */
    public Optional<Claims> parseAccessToken(String token) {
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
