package com.gfolly.quantly_backend.iam.api.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank
        String refreshToken
) {}

