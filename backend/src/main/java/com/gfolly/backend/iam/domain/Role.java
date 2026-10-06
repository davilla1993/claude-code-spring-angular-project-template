package com.gfolly.backend.iam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

import static com.gfolly.backend.iam.domain.Permission.*;

/**
 * Rôles applicatifs. Chaque rôle porte un ensemble de permissions.
 */
@RequiredArgsConstructor
@Getter
public enum Role {
    /** Administrateur : gestion des utilisateurs et consultation de l'audit. */
    ADMIN(EnumSet.of(USER_CREATE, USER_UPDATE, USER_VIEW, AUDIT_LOG_VIEW)),

    /** Utilisateur standard : accès authentifié, aucune permission d'administration. */
    USER(EnumSet.noneOf(Permission.class));

    private final Set<Permission> permissions;
}
