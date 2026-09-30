package com.gfolly.backend.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Implémentations disponibles :
 *   - {@link LocalFileStorageAdapter}  (app.storage.provider=local)
 *   - {@link MinioStorageAdapter}      (app.storage.provider=minio)
 */
public interface StoragePort {

    /**
     * Stocke le fichier et retourne son chemin/clé (ex : "tenantId/uuid.jpg").
     * Retourne null si le fichier est vide.
     */
    String store(MultipartFile file);

    /**
     * Supprime le fichier identifié par son chemin/clé.
     * Sans effet si le chemin est null ou introuvable.
     */
    void delete(String path);
}

