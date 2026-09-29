package com.gfolly.quantly_backend.iam.api.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(

        @NotBlank(message = "Le nom du commerce est requis")
        @Size(min = 3, max = 100, message = "Le nom du commerce doit contenir entre 3 et 100 caractères")
        String businessName,

        @NotBlank(message = "Le slug est requis")
        @Size(min = 3, max = 30, message = "Le slug doit contenir entre 3 et 30 caractères")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Le slug ne peut contenir que des lettres minuscules, chiffres et tirets")
        String slug,

        @NotBlank(message = "Le prénom est requis")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @NotBlank(message = "Le nom est requis")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String lastName,

        @NotBlank(message = "L'adresse e-mail est requise")
        @Email(message = "L'adresse e-mail est invalide")
        String email,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password,

        @NotBlank(message = "Le pays est requis")
        String country,

        String address,

        @NotBlank(message = "Le numéro de téléphone est requis")
        @Pattern(regexp = "^\\+?[0-9\\s-]{8,20}$", message = "Numéro de téléphone invalide")
        String phone
) {}

