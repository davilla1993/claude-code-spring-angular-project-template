package com.gfolly.backend.iam.api.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(

        @NotBlank
        String userId,

        @NotBlank
        @Size(min = 6, max = 6)
        @Pattern(regexp = "\\d{6}", message = "Le code doit être composé de 6 chiffres")
        String code
) {}

