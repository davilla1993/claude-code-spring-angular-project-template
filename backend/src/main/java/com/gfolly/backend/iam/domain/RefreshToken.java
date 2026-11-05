package com.gfolly.backend.iam.domain;

import com.gfolly.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Refresh token persisté. Seul le hash SHA-256 est stocké : une fuite de la table
 * ne permet pas de réutiliser les tokens.
 */
@Entity
@Table(name = "refresh_tokens", indexes = @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"))
@Getter
@Setter
public class RefreshToken extends BaseEntity {

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Profil actif de la session, conservé à la rotation ; null tant qu'aucun profil n'est choisi. */
    @Enumerated(EnumType.STRING)
    @Column(name = "active_role", length = 20)
    private Role activeRole;

    @Column(nullable = false)
    private boolean revoked = false;

    /** Session à laquelle appartient ce token : il ne vaut que tant qu'elle reste la session ouverte de l'utilisateur. */
    @Column(name = "session_id", length = 36)
    private String sessionId;

    public boolean belongsToSession(String currentSessionId) {
        return sessionId != null && sessionId.equals(currentSessionId);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
