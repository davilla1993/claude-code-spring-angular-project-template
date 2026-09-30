package com.gfolly.backend.shared.util;

import java.io.Serializable;

/**
 * Représente l'identité d'un utilisateur authentifié dans le SecurityContext.
 * Porte à la fois le publicId (pour les relations techniques) et l'email (pour l'audit).
 */
public record UserPrincipal(String userId, String email, String fullName, String tenantSlug) implements Serializable {
    @Override
    public String toString() {
        return userId;
    }
}

