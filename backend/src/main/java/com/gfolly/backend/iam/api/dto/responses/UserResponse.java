package com.gfolly.backend.iam.api.dto.responses;

import com.gfolly.backend.iam.domain.Role;

import java.time.LocalDateTime;

public record UserResponse(
        String publicId,
        String email,
        String username,
        String firstName,
        String lastName,
        Role role,
        Boolean active,
        Boolean emailVerified,
        Boolean firstLogin,
        Boolean multishop,
        Boolean shopCreationEnabled,
        String tenantId,
        String tenantSlug,
        String tenantName,
        String tenantLogoUrl,
        LocalDateTime createdAt,
        String cashRegisterId,
        String cashRegisterName,
        String plan
) {}


