package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stockage sur le système de fichiers local, dans {@code app.storage.uploads-dir}.
 * Les fichiers sont servis (authentifiés) sous /api/uploads/{clé} par WebConfig.
 * <p>
 * Seules les extensions de {@code app.storage.allowed-extensions} sont acceptées, ce qui empêche
 * notamment de déposer du HTML/SVG qui serait ensuite servi depuis l'origine de l'application.
 */
@Slf4j
@Component
public class LocalFileStorageAdapter implements StoragePort {

    private final Path rootLocation;
    private final Set<String> allowedExtensions;

    public LocalFileStorageAdapter(@Value("${app.storage.uploads-dir}") String uploadsDir,
                                   @Value("${app.storage.allowed-extensions}") List<String> allowedExtensions) {
        this.rootLocation = Path.of(uploadsDir).toAbsolutePath().normalize();
        this.allowedExtensions = allowedExtensions.stream()
                .map(ext -> ext.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new UncheckedIOException(ErrorMessages.STORAGE_INIT_FAILED, e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.FILE_REQUIRED);
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(ErrorMessages.UNSUPPORTED_FILE_TYPE);
        }

        String key = UUID.randomUUID() + "." + extension.toLowerCase(Locale.ROOT);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, rootLocation.resolve(key));
            return key;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new UncheckedIOException(ErrorMessages.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        Path target = rootLocation.resolve(key).normalize();
        if (!target.startsWith(rootLocation)) {
            log.warn("Refused to delete a file outside the storage root: {}", key);
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", key, e);
        }
    }
}
