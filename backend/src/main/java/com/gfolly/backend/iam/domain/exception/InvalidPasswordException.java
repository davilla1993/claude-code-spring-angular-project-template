package com.gfolly.backend.iam.domain.exception;

/**
 * Mot de passe actuel incorrect lors d'une opération authentifiée (400, pas 401 :
 * la session reste valide, seule la saisie est fausse).
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
