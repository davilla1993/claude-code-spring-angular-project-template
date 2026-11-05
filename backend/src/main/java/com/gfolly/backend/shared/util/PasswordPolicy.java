package com.gfolly.backend.shared.util;

/**
 * Politique des mots de passe : 8 à 25 caractères, au moins une majuscule, une minuscule, un chiffre
 * et un caractère spécial. Miroir côté frontend : core/validators/password-validators.ts.
 */
public final class PasswordPolicy {

    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_+=\\[\\]{};':\"\\\\|,.<>/?]).{8,25}$";

    public static final String MESSAGE =
            "Le mot de passe doit contenir entre 8 et 25 caractères, dont une majuscule, une minuscule, un chiffre et "
                    + "un caractère spécial.";

    private PasswordPolicy() {}
}
