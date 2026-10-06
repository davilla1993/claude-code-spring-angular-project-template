package com.gfolly.backend.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Port de stockage de fichiers. Implémentation fournie : {@link LocalFileStorageAdapter}.
 * Pour un stockage objet (S3, MinIO...), ajouter un nouvel adaptateur implémentant ce port.
 */
public interface StoragePort {

    /**
     * Stocke le fichier sous un nom généré et retourne sa clé (ex : "3f2a...c1.png").
     *
     * @throws IllegalArgumentException si le fichier est vide ou si son extension n'est pas autorisée
     */
    String store(MultipartFile file);

    /**
     * Supprime le fichier identifié par sa clé. Sans effet si la clé est vide ou introuvable.
     */
    void delete(String key);
}
