package com.gfolly.backend.shared.util;

/**
 * Règle des prénoms et noms : 2 à 25 caractères, sans compter les espaces de début et de fin, retirés à l'enregistrement.
 * Miroir côté frontend : core/validators/person-name-validators.ts.
 */
public final class PersonName {

    public static final String PATTERN = "^\\s*\\S.{0,23}\\S\\s*$";

    public static final String MESSAGE = "Ce champ doit contenir entre 2 et 25 caractères.";

    private PersonName() {}
}
