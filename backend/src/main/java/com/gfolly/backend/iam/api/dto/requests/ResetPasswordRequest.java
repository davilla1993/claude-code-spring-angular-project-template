package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.shared.util.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 6) @Pattern(regexp = "\\d{6}") String code,
        @NotBlank @Pattern(regexp = PasswordStrength.PATTERN, message = PasswordStrength.MESSAGE) String newPassword
) {}

