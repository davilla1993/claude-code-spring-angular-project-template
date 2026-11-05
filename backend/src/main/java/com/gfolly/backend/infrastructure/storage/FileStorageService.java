package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.shared.util.ErrorMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Point d'entrée du stockage pour le code de l'application. Il porte les contrôles communs, qui restent
 * donc actifs quel que soit le fournisseur branché sur {@link StoragePort}.
 * <p>
 * Seules les extensions de {@code app.storage.allowed-extensions} sont acceptées, ce qui empêche
 * notamment de déposer du HTML/SVG qui serait ensuite servi depuis l'origine de l'application.
 */
@Service
public class FileStorageService {

    private static final Pattern KEY_PATTERN =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.([a-z0-9]+)");

    private final StoragePort storage;
    private final Set<String> allowedExtensions;

    public FileStorageService(StoragePort storage,
                              @Value("${app.storage.allowed-extensions}") List<String> allowedExtensions) {
        this.storage = storage;
        this.allowedExtensions = allowedExtensions.stream()
                .map(ext -> ext.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Conserve le fichier sous un nom généré et retourne sa clé (ex : "3f2a...c1.png").
     *
     * @throws IllegalArgumentException si le fichier est vide ou si son extension n'est pas autorisée
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.FILE_REQUIRED);
        }

        String key = UUID.randomUUID() + "." + allowedExtensionOf(file);
        try (InputStream content = file.getInputStream()) {
            callProvider(() -> {
                storage.store(key, content, file.getSize());
                return key;
            });
            return key;
        } catch (IOException e) {
            throw new StorageException(ErrorMessages.FILE_UPLOAD_FAILED, e);
        }
    }

    /**
     * Lit un fichier. Une clé mal formée n'atteint jamais le fournisseur.
     *
     * @throws StoredFileNotFoundException si la clé est invalide ou si le fichier est introuvable
     */
    public StoredFile load(String key) {
        if (!isValidKey(key)) {
            throw new StoredFileNotFoundException();
        }
        return callProvider(() -> storage.load(key)).orElseThrow(StoredFileNotFoundException::new);
    }

    /**
     * Supprime un fichier. Sans effet si la clé est invalide ou si le fichier est introuvable.
     */
    public void delete(String key) {
        if (isValidKey(key)) {
            callProvider(() -> {
                storage.delete(key);
                return key;
            });
        }
    }

    /** Une erreur inattendue du fournisseur devient une panne : son message ne doit jamais atteindre le client. */
    private <T> T callProvider(Supplier<T> call) {
        try {
            return call.get();
        } catch (StorageException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new StorageException("Unexpected storage provider error", e);
        }
    }

    private String allowedExtensionOf(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !allowedExtensions.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(ErrorMessages.UNSUPPORTED_FILE_TYPE);
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private boolean isValidKey(String key) {
        if (key == null) {
            return false;
        }
        var matcher = KEY_PATTERN.matcher(key);
        return matcher.matches() && allowedExtensions.contains(matcher.group(1));
    }
}
