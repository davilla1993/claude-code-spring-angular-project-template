package com.gfolly.backend.shared.util;

import java.io.Serializable;

/**
 * Identité d'un utilisateur authentifié dans le SecurityContext.
 * Porte le publicId (relations techniques), l'email (audit) et le profil actif
 * (nom du rôle ; null tant qu'aucun profil n'est choisi).
 */
public record UserPrincipal(String userId, String email, String fullName, String activeRole) implements Serializable {
    @Override
    public String toString() {
        return userId;
    }
}
