package com.gfolly.backend.iam.domain.exception;

/**
 * Opération refusée par une règle métier (ex : un utilisateur qui modifie son propre rôle).
 */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
