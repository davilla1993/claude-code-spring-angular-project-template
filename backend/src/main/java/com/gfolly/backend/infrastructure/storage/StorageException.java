package com.gfolly.backend.infrastructure.storage;

/** Panne du stockage, quel que soit le fournisseur. Le détail reste dans les logs, jamais dans la réponse. */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
