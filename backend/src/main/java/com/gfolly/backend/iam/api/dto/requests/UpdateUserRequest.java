package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.shared.util.PersonName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record UpdateUserRequest(
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String firstName,
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String lastName,
        @NotEmpty Set<@NotNull Role> roles
) {}
