package com.gfolly.backend.iam.infrastructure.mapper.response;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Permission;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;

import java.util.List;

public final class UserResponseMapper {

    /** Hors session (administration, inscription) : ni profil actif ni permissions. */
    public static UserResponse toResponse(User user) {
        return toResponse(user, null);
    }

    /** Pour une session : seules les permissions du profil actif sont exposées, jamais le cumul des rôles. */
    public static UserResponse toResponse(User user, Role activeRole) {
        return new UserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream().sorted().toList(),
                activeRole,
                activeRole != null
                        ? activeRole.getPermissions().stream().map(Permission::getCode).sorted().toList()
                        : List.of(),
                user.isActive(),
                user.isEmailVerified(),
                user.isFirstLogin(),
                user.getCreatedAt()
        );
    }

    private UserResponseMapper() {}
}
