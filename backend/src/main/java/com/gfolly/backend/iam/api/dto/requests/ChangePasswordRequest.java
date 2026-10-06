package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.shared.util.PasswordStrength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(max = 72) String currentPassword,
        @NotBlank @Pattern(regexp = PasswordStrength.PATTERN, message = PasswordStrength.MESSAGE) String newPassword
) {}
