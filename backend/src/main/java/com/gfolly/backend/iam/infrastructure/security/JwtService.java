package com.gfolly.quantly_backend.iam.infrastructure.security;

import com.gfolly.quantly_backend.iam.domain.Permission;
import com.gfolly.quantly_backend.iam.domain.RefreshToken;
import com.gfolly.quantly_backend.iam.domain.Role;
import com.gfolly.quantly_backend.iam.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(User user, String tenantSlug) {
        return generateAccessToken(user, tenantSlug, null);
    }

    public String generateAccessToken(User user, String tenantSlug, String impersonatorId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        List<String> permissions = user.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());

        var claims = new java.util.HashMap<String, Object>(Map.of(
                "tenantId", user.getTenantId() != null ? user.getTenantId() : "SYSTEM",
                "tenantSlug", tenantSlug != null ? tenantSlug : "",
                "role", user.getRole().name(),
                "permissions", permissions,
                "email", user.getEmail(),
                "fullName", user.getFirstName() + " " + user.getLastName()
        ));

        if (impersonatorId != null) {
            claims.put("imp_src", impersonatorId);
        }

        return Jwts.builder()
                .subject(user.getPublicId())
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public RefreshToken generateRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(user.getPublicId());
        token.setTenantId(user.getTenantId());
        token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        return token;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractFullName(String token) {
        return extractAllClaims(token).get("fullName", String.class);
    }

    public String extractTenantId(String token) {
        return extractAllClaims(token).get("tenantId", String.class);
    }

    public String extractTenantSlug(String token) {
        return extractAllClaims(token).get("tenantSlug", String.class);
    }

    public Role extractRole(String token) {
        String role = extractAllClaims(token).get("role", String.class);
        return Role.valueOf(role);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        return extractAllClaims(token).get("permissions", List.class);
    }

    /**
     * Retourne l'ID du Commander à l'origine d'une session d'impersonation,
     * ou null si le token n'est pas un token d'impersonation.
     */
    public String extractImpersonatorId(String token) {
        return extractAllClaims(token).get("imp_src", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}

