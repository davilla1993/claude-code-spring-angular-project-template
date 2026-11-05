package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.shared.util.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetupPasswordRequest(
        @NotBlank @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.MESSAGE) String newPassword
) {}

