package com.gfolly.backend.infrastructure.storage;

import java.io.InputStream;
import java.util.Optional;

/**
 * Port de stockage de fichiers. Implémentation fournie : {@link LocalFileStorageAdapter}.
 * Pour un stockage objet (S3, MinIO...), ajouter un adaptateur qui passe {@code StoragePortContractTest}.
 * <p>
 * Le port ne fait que conserver des octets : les contrôles sont dans {@link FileStorageService}, que
 * le code de l'application doit utiliser à la place de ce port. Toute panne du fournisseur est levée
 * en {@link StorageException}.
 */
public interface StoragePort {

    /**
     * Conserve le contenu sous cette clé, sans jamais écraser un fichier existant.
     *
     * @param size taille du contenu en octets
     * @throws StorageException si la clé existe déjà ou en cas de panne ; aucun fichier partiel ne reste lisible
     */
    void store(String key, InputStream content, long size);

    /**
     * Lit le fichier identifié par sa clé. L'appelant doit fermer le flux retourné.
     *
     * @return le fichier, ou vide s'il est introuvable
     */
    Optional<StoredFile> load(String key);

    /**
     * Supprime le fichier identifié par sa clé. Sans effet s'il est introuvable.
     */
    void delete(String key);
}
