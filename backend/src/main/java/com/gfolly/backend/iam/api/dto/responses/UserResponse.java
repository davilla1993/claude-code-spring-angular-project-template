package com.gfolly.backend.iam.api.dto.responses;

import com.gfolly.backend.iam.domain.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @param roles       rôles détenus par l'utilisateur, triés.
 * @param activeRole  profil actif de la session ; null hors session ou tant qu'aucun profil n'est choisi.
 * @param permissions codes des permissions du profil actif (ex : "user:view"), pour adapter l'interface ;
 *                    jamais le cumul de plusieurs rôles. L'autorisation reste vérifiée côté serveur.
 */
public record UserResponse(
        String publicId,
        String email,
        String firstName,
        String lastName,
        List<Role> roles,
        Role activeRole,
        List<String> permissions,
        boolean active,
        boolean emailVerified,
        boolean firstLogin,
        LocalDateTime createdAt
) {}
