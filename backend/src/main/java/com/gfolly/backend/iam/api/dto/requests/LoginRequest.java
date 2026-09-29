package com.gfolly.quantly_backend.iam.api.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String email,

        @NotBlank
        String password,

        /** Null ou vide = portail OWNER. Non-null = connexion depuis le sous-domaine (employés). */
        String subdomain
) {}

