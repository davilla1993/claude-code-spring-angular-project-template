package com.gfolly.backend.shared.util;

import java.io.Serializable;

/**
 * Identité d'un utilisateur authentifié dans le SecurityContext.
 * Porte le publicId (relations techniques) et l'email (audit).
 */
public record UserPrincipal(String userId, String email, String fullName) implements Serializable {
    @Override
    public String toString() {
        return userId;
    }
}
