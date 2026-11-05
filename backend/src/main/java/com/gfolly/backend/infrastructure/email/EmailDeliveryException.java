package com.gfolly.backend.infrastructure.email;

/** Échec d'envoi d'un email, quel que soit le fournisseur. */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
