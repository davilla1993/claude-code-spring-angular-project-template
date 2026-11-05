package com.gfolly.backend.iam.domain;

import com.gfolly.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
// DynamicUpdate : une écriture concurrente ne doit pas restaurer une ancienne valeur de currentSessionId.
@DynamicUpdate
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

    /** Chargés systématiquement ; le lot évite le N+1 sur les listes paginées. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @BatchSize(size = 50)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /** Vrai tant que l'utilisateur n'a pas remplacé le mot de passe temporaire fourni par un administrateur. */
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin = false;

    /** Seule session ouverte de l'utilisateur ; null si aucune. Vérifiée à chaque requête authentifiée. */
    @Column(name = "current_session_id", length = 36)
    private String currentSessionId;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Le rôle de l'utilisateur s'il n'en a qu'un ; vide sinon : les permissions de plusieurs rôles ne se cumulent jamais. */
    public Optional<Role> soleRole() {
        return roles.size() == 1 ? Optional.of(roles.iterator().next()) : Optional.empty();
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
