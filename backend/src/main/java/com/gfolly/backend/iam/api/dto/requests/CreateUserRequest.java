package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.shared.util.PasswordStrength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Création d'un compte par un administrateur. Le mot de passe est temporaire :
 * l'utilisateur devra le remplacer à sa première connexion.
 */
public record CreateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = PasswordStrength.PATTERN, message = PasswordStrength.MESSAGE) String temporaryPassword,
        @NotNull Role role
) {}
