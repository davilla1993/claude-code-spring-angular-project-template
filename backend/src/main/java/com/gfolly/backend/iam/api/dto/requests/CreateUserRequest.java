package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.shared.util.PasswordPolicy;
import com.gfolly.backend.shared.util.PersonName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Création d'un compte par un administrateur. Le mot de passe est temporaire :
 * l'utilisateur devra le remplacer à sa première connexion.
 */
public record CreateUserRequest(
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String firstName,
        @NotBlank @Pattern(regexp = PersonName.PATTERN, message = PersonName.MESSAGE) String lastName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = PasswordPolicy.PATTERN, message = PasswordPolicy.MESSAGE) String temporaryPassword,
        @NotEmpty Set<@NotNull Role> roles
) {}
