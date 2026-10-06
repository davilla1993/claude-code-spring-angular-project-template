package com.gfolly.backend.iam.infrastructure.mapper.response;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Permission;
import com.gfolly.backend.iam.domain.User;

public final class UserResponseMapper {

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getRole().getPermissions().stream().map(Permission::getCode).sorted().toList(),
                user.isActive(),
                user.isEmailVerified(),
                user.isFirstLogin(),
                user.getCreatedAt()
        );
    }

    private UserResponseMapper() {}
}
