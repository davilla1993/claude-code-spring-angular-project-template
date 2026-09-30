package com.gfolly.backend.shared.util;

/**
 * Règles de robustesse : min 6 caractères, une majuscule, une minuscule, un chiffre, un caractère spécial.
 */
public class PasswordStrength {

    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_+=\\[\\]{};':\"\\\\|,.<>/?]).{6,}$";

    public static final String MESSAGE =
            "Le mot de passe doit contenir au moins 6 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.";

    private PasswordStrength() {}
}

