package com.gfolly.backend.iam.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank @Size(max = 36) String userId,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code doit être composé de 6 chiffres") String code
) {}
