package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.Role;
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
 * Le token porte le profil actif ; les permissions ne sont pas embarquées : elles sont dérivées de ce profil à chaque requête.
 */
@Service
@Slf4j
public class JwtService {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SESSION = "sid";

    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpirationMs) {
        // Lève WeakKeyException si le secret fait moins de 256 bits : échec au démarrage plutôt qu'en production.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * @param activeRole profil actif de la session ; null tant qu'aucun profil n'est choisi (token sans permission)
     */
    public String generateAccessToken(User user, Role activeRole) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(user.getPublicId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_FULL_NAME, user.getFullName())
                .claim(CLAIM_SESSION, user.getCurrentSessionId());
        if (activeRole != null) {
            builder.claim(CLAIM_ROLE, activeRole.name());
        }
        return builder
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
