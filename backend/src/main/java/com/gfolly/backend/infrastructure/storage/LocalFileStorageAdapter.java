package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

/**
 * Stockage sur le système de fichiers local, dans {@code app.storage.uploads-dir}.
 * Tout accès est confiné à ce répertoire.
 */
@Slf4j
@Component
public class LocalFileStorageAdapter implements StoragePort {

    private final Path rootLocation;

    public LocalFileStorageAdapter(@Value("${app.storage.uploads-dir}") String uploadsDir) {
        this.rootLocation = Path.of(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException(ErrorMessages.STORAGE_INIT_FAILED, e);
        }
    }

    @Override
    public void store(String key, InputStream content, long size) {
        Path target = resolve(key).orElseThrow(() -> new IllegalArgumentException(ErrorMessages.FILE_UPLOAD_FAILED));
        try {
            Files.copy(content, target);
        } catch (FileAlreadyExistsException e) {
            throw new StorageException(ErrorMessages.FILE_UPLOAD_FAILED, e);
        } catch (IOException e) {
            removePartialFile(target);
            throw new StorageException(ErrorMessages.FILE_UPLOAD_FAILED, e);
        }
    }

    @Override
    public Optional<StoredFile> load(String key) {
        Optional<Path> target = resolve(key);
        if (target.isEmpty()) {
            return Optional.empty();
        }
        try {
            BasicFileAttributes attributes = Files.readAttributes(target.get(), BasicFileAttributes.class);
            return attributes.isRegularFile()
                    ? Optional.of(new StoredFile(Files.newInputStream(target.get()), attributes.size()))
                    : Optional.empty();
        } catch (NoSuchFileException e) {
            // Sans répertoire de stockage, c'est une panne et non un fichier absent.
            if (Files.isDirectory(rootLocation)) {
                return Optional.empty();
            }
            throw new StorageException("Storage root is unavailable", e);
        } catch (IOException e) {
            throw new StorageException("Could not read a stored file", e);
        }
    }

    @Override
    public void delete(String key) {
        Optional<Path> target = resolve(key);
        if (target.isEmpty()) {
            return;
        }
        try {
            Files.deleteIfExists(target.get());
        } catch (IOException e) {
            throw new StorageException("Could not delete a stored file", e);
        }
    }

    private void removePartialFile(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not remove a partial file", e);
        }
    }

    /** Chemin du fichier, ou vide si la clé est invalide ou sort du répertoire de stockage. */
    private Optional<Path> resolve(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        try {
            Path target = rootLocation.resolve(key).normalize();
            if (target.startsWith(rootLocation) && !target.equals(rootLocation)) {
                return Optional.of(target);
            }
        } catch (InvalidPathException e) {
            log.warn("Refused an invalid storage key");
            return Optional.empty();
        }
        log.warn("Refused a storage key outside the storage root");
        return Optional.empty();
    }
}
