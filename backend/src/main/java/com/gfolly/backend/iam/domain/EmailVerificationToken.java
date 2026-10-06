package com.gfolly.backend.iam.domain;

import com.gfolly.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_tokens", indexes = @Index(name = "idx_email_verif_user_id", columnList = "user_id"))
@Getter
@Setter
public class EmailVerificationToken extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /** Code à 6 chiffres envoyé par email. */
    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    /** Comparaison en temps constant pour ne pas exposer d'information par timing. */
    public boolean matches(String candidate) {
        return candidate != null && MessageDigest.isEqual(
                code.getBytes(StandardCharsets.UTF_8), candidate.getBytes(StandardCharsets.UTF_8));
    }
}
