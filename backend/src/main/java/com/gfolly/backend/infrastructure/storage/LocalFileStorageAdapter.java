package com.gfolly.quantly_backend.infrastructure.storage;

import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Adaptateur de stockage local : écrit les fichiers sur le système de fichiers
 * du serveur, dans {@code uploads/{tenantId}/{uuid}.ext}.
 * Actif quand {@code app.storage.provider=local} (valeur par défaut).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageAdapter implements StoragePort {

    private final Path rootLocation;

    public LocalFileStorageAdapter(@Value("${app.uploads-dir:./uploads}") String uploadsDir) {
        this.rootLocation = Paths.get(uploadsDir);
        init();
    }

    private void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            log.error("Storage initialization failed", e);
            throw new RuntimeException(ErrorMessages.STORAGE_INIT_FAILED, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException(ErrorMessages.TENANT_CONTEXT_MISSING);
        }

        try {
            Path tenantLocation = this.rootLocation.resolve(tenantId);
            if (!Files.exists(tenantLocation)) {
                Files.createDirectories(tenantLocation);
            }

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");

            Path targetLocation = tenantLocation.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return tenantId + "/" + filename;

        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException(ErrorMessages.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void delete(String path) {
        if (path == null || path.isBlank()) {
            return;
        }

        try {
            Path fileToDelete = this.rootLocation.resolve(path);
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", path, e);
        }
    }
}
