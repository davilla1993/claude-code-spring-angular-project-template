package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-0123456789-abcdefghijklmnopqrstuvwxyz";

    private final JwtService jwtService = new JwtService(SECRET, 60_000);

    @Test
    void generatedTokenCanBeParsedBack() {
        User user = user();

        Claims claims = jwtService.parseAccessToken(jwtService.generateAccessToken(user, Role.ADMIN)).orElseThrow();

        assertThat(claims.getSubject()).isEqualTo(user.getPublicId());
        assertThat(claims.get(JwtService.CLAIM_EMAIL, String.class)).isEqualTo("jane@example.com");
        assertThat(claims.get(JwtService.CLAIM_ROLE, String.class)).isEqualTo("ADMIN");
        assertThat(claims.get(JwtService.CLAIM_FULL_NAME, String.class)).isEqualTo("Jane Doe");
    }

    @Test
    void tokenHasNoRoleClaimWithoutActiveProfile() {
        User user = user();
        user.getRoles().add(Role.USER);

        Claims claims = jwtService.parseAccessToken(jwtService.generateAccessToken(user, null)).orElseThrow();

        assertThat(claims).doesNotContainKey(JwtService.CLAIM_ROLE);
        assertThat(claims.getSubject()).isEqualTo(user.getPublicId());
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService other = new JwtService("another-secret-0123456789-abcdefghijklmnopqrstuvwxyz", 60_000);

        assertThat(jwtService.parseAccessToken(other.generateAccessToken(user(), Role.ADMIN))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService expiring = new JwtService(SECRET, -1_000);

        assertThat(jwtService.parseAccessToken(expiring.generateAccessToken(user(), Role.ADMIN))).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(jwtService.parseAccessToken("not-a-jwt")).isEmpty();
        assertThat(jwtService.parseAccessToken("")).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService("too-short", 60_000)).isInstanceOf(WeakKeyException.class);
    }

    private static User user() {
        User user = new User();
        user.setEmail("jane@example.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.getRoles().add(Role.ADMIN);
        return user;
    }
}
