package com.gfolly.backend.iam.api.dto.responses;

import com.gfolly.backend.iam.domain.Role;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @param permissions codes des permissions du rôle (ex : "user:view"), pour adapter l'interface.
 *                    L'autorisation reste vérifiée côté serveur.
 */
public record UserResponse(
        String publicId,
        String email,
        String firstName,
        String lastName,
        Role role,
        List<String> permissions,
        boolean active,
        boolean emailVerified,
        boolean firstLogin,
        LocalDateTime createdAt
) {}
