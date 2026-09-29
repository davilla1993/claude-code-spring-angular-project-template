package com.gfolly.quantly_backend.iam.api.dto.requests;

import com.gfolly.quantly_backend.iam.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotNull
        Role role
) {}

