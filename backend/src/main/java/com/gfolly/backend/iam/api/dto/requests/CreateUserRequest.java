package com.gfolly.quantly_backend.iam.api.dto.requests;

import com.gfolly.quantly_backend.iam.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        @Email
        String email,

        /** Nom d'utilisateur unique dans le tenant, requis pour les employés. */
        @NotBlank
        @jakarta.validation.constraints.Size(min = 3, max = 30)
        @jakarta.validation.constraints.Pattern(regexp = "^[a-z0-9._-]+$", message = "Lettres minuscules, chiffres, '.', '-', '_' uniquement")
        String username,

        @NotBlank
        @jakarta.validation.constraints.Size(min = 8)
        String password,

        @NotNull
        Role role
) {}

