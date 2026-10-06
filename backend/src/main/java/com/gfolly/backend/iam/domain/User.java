package com.gfolly.backend.iam.domain;

import com.gfolly.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    /** Toujours stocké normalisé (voir {@link #normalizeEmail(String)}). */
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /** Vrai tant que l'utilisateur n'a pas remplacé le mot de passe temporaire fourni par un administrateur. */
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin = false;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
