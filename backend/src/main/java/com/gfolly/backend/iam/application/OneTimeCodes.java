package com.gfolly.backend.iam.application;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Codes à usage unique envoyés par email (vérification d'adresse, réinitialisation du mot de passe).
 */
final class OneTimeCodes {

    static final int VALIDITY_MINUTES = 15;

    private static final SecureRandom RANDOM = new SecureRandom();

    private OneTimeCodes() {}

    /** Code à 6 chiffres (100000–999999). */
    static String generate() {
        return String.valueOf(RANDOM.nextInt(900_000) + 100_000);
    }

    static LocalDateTime expiry() {
        return LocalDateTime.now().plusMinutes(VALIDITY_MINUTES);
    }
}
