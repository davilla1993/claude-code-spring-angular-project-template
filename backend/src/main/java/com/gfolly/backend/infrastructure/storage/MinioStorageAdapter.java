package com.gfolly.backend.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adaptateur de stockage MinIO (compatible S3).
 * Actif quand {@code app.storage.provider=minio}.
 *
 * TODO : implémenter avec le SDK MinIO :
 *   <dependency>
 *     <groupId>io.minio</groupId>
 *     <artifactId>minio</artifactId>
 *     <version>8.5.x</version>
 *   </dependency>
 *
 * Variables d'environnement attendues :
 *   MINIO_ENDPOINT   (ex : http://localhost:9000)
 *   MINIO_ACCESS_KEY
 *   MINIO_SECRET_KEY
 *   MINIO_BUCKET     (ex : quantly-uploads)
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "minio")
public class MinioStorageAdapter implements StoragePort {

    @Override
    public String store(MultipartFile file) {
        throw new UnsupportedOperationException(
                "MinioStorageAdapter non encore implémenté. " +
                "Passez app.storage.provider=local ou implémentez cet adaptateur."
        );
    }

    @Override
    public void delete(String path) {
        throw new UnsupportedOperationException(
                "MinioStorageAdapter non encore implémenté. " +
                "Passez app.storage.provider=local ou implémentez cet adaptateur."
        );
    }
}

