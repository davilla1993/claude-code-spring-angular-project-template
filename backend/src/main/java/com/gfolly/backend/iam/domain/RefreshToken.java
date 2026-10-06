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

    @Column(nullable = false)
    private boolean revoked = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
