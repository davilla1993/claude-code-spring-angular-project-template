package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotEmpty Set<@NotNull Role> roles
) {}
