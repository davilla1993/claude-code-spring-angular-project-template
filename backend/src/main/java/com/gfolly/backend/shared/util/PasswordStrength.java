package com.gfolly.backend.shared.util;

/**
 * Règles de robustesse : 8 à 72 caractères, une majuscule, une minuscule, un chiffre, un caractère spécial.
 * La borne haute correspond à la limite de 72 octets de BCrypt.
 */
public final class PasswordStrength {

    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_+=\\[\\]{};':\"\\\\|,.<>/?]).{8,72}$";

    public static final String MESSAGE =
            "Le mot de passe doit contenir entre 8 et 72 caractères, dont une majuscule, une minuscule, un chiffre et "
                    + "un caractère spécial.";

    private PasswordStrength() {}
}
