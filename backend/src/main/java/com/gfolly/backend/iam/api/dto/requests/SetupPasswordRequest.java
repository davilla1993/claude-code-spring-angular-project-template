package com.gfolly.quantly_backend.iam.api.dto.requests;

import com.gfolly.quantly_backend.shared.util.PasswordStrength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetupPasswordRequest(
        @NotBlank @Pattern(regexp = PasswordStrength.PATTERN, message = PasswordStrength.MESSAGE) String newPassword
) {}
