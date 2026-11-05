package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.shared.util.PasswordPolicy;
import com.gfolly.backend.shared.util.PersonName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String firstName,
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.MESSAGE) String password
) {}
