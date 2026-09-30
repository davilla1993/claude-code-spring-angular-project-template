package com.gfolly.backend.iam.infrastructure.mapper;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.User;

public class UserMapper {

    public static UserResponse toResponse(User user, String tenantSlug, String tenantName, String tenantLogoUrl, String plan) {
        return new UserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getActive(),
                user.getEmailVerified(),
                user.getFirstLogin(),
                user.getMultishop(),
                user.getShopCreationEnabled(),
                user.getTenantId(),
                tenantSlug,
                tenantName,
                tenantLogoUrl,
                user.getCreatedAt(),
                null,
                null,
                plan
        );
    }

    /** Surcharge avec slug uniquement — compatibilité endpoints gestion (liste, CRUD employés). */
    public static UserResponse toResponse(User user, String tenantSlug) {
        return toResponse(user, tenantSlug, null, null, null);
    }

    /** Surcharge sans contexte tenant — pour les endpoints de gestion internes. */
    public static UserResponse toResponse(User user) {
        return toResponse(user, null, null, null, null);
    }

    private UserMapper() {}
}


