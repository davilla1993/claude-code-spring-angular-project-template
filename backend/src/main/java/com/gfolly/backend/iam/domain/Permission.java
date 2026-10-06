package com.gfolly.backend.iam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Permissions fines, utilisées dans les @PreAuthorize("hasAuthority('...')").
 * Étendre cette liste avec les permissions propres à chaque projet.
 */
@RequiredArgsConstructor
@Getter
public enum Permission {
    USER_CREATE("user:create", "Créer des utilisateurs"),
    USER_UPDATE("user:update", "Modifier des utilisateurs"),
    USER_VIEW("user:view", "Voir les utilisateurs"),
    AUDIT_LOG_VIEW("audit:log:view", "Voir les journaux d'audit");

    private final String code;
    private final String description;
}
