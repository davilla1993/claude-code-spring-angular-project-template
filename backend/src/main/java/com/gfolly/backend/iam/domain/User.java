package com.gfolly.backend.iam.domain;

import com.gfolly.backend.shared.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_email_tenant", columnNames = { "email", "tenant_id" }),
        @UniqueConstraint(name = "uq_users_username_tenant", columnNames = { "username", "tenant_id" })
    }
)
@Getter
@Setter
public class User extends TenantAwareEntity {

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /** Indique si l'utilisateur (OWNER) a le droit de gérer plusieurs boutiques. */
    @Column(name = "multishop", nullable = false, columnDefinition = "boolean default false")
    private Boolean multishop = false;

    /** Indique si l'utilisateur (OWNER) est autorisé à créer une nouvelle boutique. Levier de facturation contrôlé par COMMANDER, indépendant de {@link #multishop}. */
    @Column(name = "shop_creation_enabled", nullable = false, columnDefinition = "boolean default false")
    private Boolean shopCreationEnabled = false;

    /** Nom d'utilisateur unique dans le tenant — utilisé pour la connexion des employés. */
    @Column(name = "username")
    private String username;

    /** Vrai tant que l'employé n'a pas défini son propre mot de passe. Toujours false pour OWNER. */
    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin = false;

    public String getFullName() {
        if (firstName == null && lastName == null) return username != null ? username : email;
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}


