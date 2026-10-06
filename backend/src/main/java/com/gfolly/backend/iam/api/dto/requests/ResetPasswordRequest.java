package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.shared.util.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code doit être composé de 6 chiffres") String code,
        @NotBlank @Pattern(regexp = PasswordStrength.PATTERN, message = PasswordStrength.MESSAGE) String newPassword
) {}
