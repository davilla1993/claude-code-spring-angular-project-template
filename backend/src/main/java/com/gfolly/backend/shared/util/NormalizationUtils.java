package com.gfolly.backend.shared.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utilitaire de normalisation des chaînes de caractères.
 */
public class NormalizationUtils {

    private static final Pattern DECOMB_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALPHANUM_PATTERN = Pattern.compile("[^a-z0-9 ]");
    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Nettoie une chaîne pour l'affichage (Trim + espaces multiples).
     */
    public static String normalizeForDisplay(String input) {
        if (input == null) return null;
        return MULTI_SPACE_PATTERN.matcher(input.trim()).replaceAll(" ");
    }

    /**
     * Normalise une chaîne pour la comparaison/recherche:
     * - Trim
     * - Minuscules
     * - Suppression des accents
     * - Suppression des caractères spéciaux (garde uniquement a-z0-9 et espaces)
     * - Nettoyage des espaces multiples
     */
    public static String normalizeBySearch(String input) {
        if (input == null) return null;

        // 1. Mise en minuscule et trim
        String normalized = input.trim().toLowerCase();

        // 2. Suppression des accents
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = DECOMB_PATTERN.matcher(normalized).replaceAll("");

        // 3. Suppression des caractères spéciaux (remplacés par rien)
        normalized = NON_ALPHANUM_PATTERN.matcher(normalized).replaceAll("");

        // 4. Nettoyage des espaces multiples
        normalized = MULTI_SPACE_PATTERN.matcher(normalized).replaceAll(" ");

        return normalized.trim();
    }

    /**
     * Formate un nom de famille en majuscules (ex: "adjanta" -> "ADJANTA").
     */
    public static String formatLastName(String input) {
        if (input == null) return null;
        return input.trim().toUpperCase();
    }

    /**
     * Formate un prénom en Title Case (ex: "amélé sandrine" -> "Amélé Sandrine").
     * Gère les espaces et les traits d'union.
     */
    public static String formatFirstName(String input) {
        if (input == null) return null;
        String trimmed = input.trim().toLowerCase();
        if (trimmed.isEmpty()) return trimmed;

        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : trimmed.toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-') {
                nextTitleCase = true;
                sb.append(c);
            } else if (nextTitleCase) {
                sb.append(Character.toUpperCase(c));
                nextTitleCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}

