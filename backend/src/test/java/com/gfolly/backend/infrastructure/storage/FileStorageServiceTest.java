package com.gfolly.backend.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Vérifie les contrôles communs avec un faux fournisseur : ils ne dépendent d'aucun stockage réel. */
class FileStorageServiceTest {

    private static final String VALID_KEY = "3f2a0000-0000-4000-8000-0000000000c1.png";

    private InMemoryStorage storage;
    private FileStorageService service;

    @BeforeEach
    void setUp() {
        storage = new InMemoryStorage();
        service = new FileStorageService(storage, List.of("png", " PDF "));
    }

    @Test
    void storesAllowedFileUnderGeneratedName() {
        String key = service.store(new MockMultipartFile("file", "Photo.PNG", "image/png", new byte[]{1, 2, 3}));

        assertThat(key).matches("[0-9a-f-]{36}\\.png");
        assertThat(storage.files.get(key)).containsExactly(1, 2, 3);
    }

    @Test
    void acceptsExtensionsConfiguredWithSpacesOrUppercase() {
        assertThat(service.store(new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1}))).endsWith(".pdf");
    }

    @Test
    void ignoresTheClientContentTypeAndFileName() {
        String key = service.store(new MockMultipartFile("file", "../../evil.png", "text/html", new byte[]{1}));

        assertThat(key).matches("[0-9a-f-]{36}\\.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {"x.html", "x.svg", "x.png.html", "noext"})
    void rejectsDisallowedOrMissingExtension(String name) {
        assertThatThrownBy(() -> service.store(new MockMultipartFile("file", name, "image/png", new byte[]{1})))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(storage.files).isEmpty();
    }

    @Test
    void rejectsEmptyOrMissingFile() {
        assertThatThrownBy(() -> service.store(new MockMultipartFile("file", "x.png", "image/png", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.store(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loadsAStoredFile() throws IOException {
        String key = service.store(new MockMultipartFile("file", "a.png", "image/png", new byte[]{7}));

        try (InputStream content = service.load(key).content()) {
            assertThat(content.readAllBytes()).containsExactly(7);
        }
    }

    @Test
    void loadFailsWhenTheFileDoesNotExist() {
        assertThatThrownBy(() -> service.load(VALID_KEY)).isInstanceOf(StoredFileNotFoundException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "",
            "../secret.png",
            "..\\secret.png",
            "secret.png",
            "3f2a0000-0000-4000-8000-0000000000c1.html",
            "3f2a0000-0000-4000-8000-0000000000c1.PNG",
            "3f2a0000-0000-4000-8000-0000000000c1"
    })
    void invalidKeysNeverReachTheProvider(String key) {
        assertThatThrownBy(() -> service.load(key)).isInstanceOf(StoredFileNotFoundException.class);

        service.delete(key);

        assertThat(storage.calls).isZero();
    }

    @Test
    void deletesAStoredFile() {
        String key = service.store(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}));

        service.delete(key);

        assertThat(storage.files).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "x.", "x.png ", "x.png/", "x.html\0", "x.png\\"})
    void rejectsHostileFileNames(String name) {
        assertThatThrownBy(() -> service.store(new MockMultipartFile("file", name, "image/png", new byte[]{1})))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(storage.calls).isZero();
    }

    @Test
    void doubleExtensionKeepsOnlyTheAllowedOne() {
        String key = service.store(new MockMultipartFile("file", "x.html.png", "text/html", new byte[]{1}));

        assertThat(key).matches("[0-9a-f-]{36}\\.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "3f2a0000-0000-4000-8000-0000000000c1.png\n",
            "3f2a0000-0000-4000-8000-0000000000c1.png ",
            "3f2a0000-0000-4000-8000-0000000000c1.png\0.html",
            "3f2a0000-0000-4000-8000-0000000000c1.png/../../secret.png",
            "3F2A0000-0000-4000-8000-0000000000C1.png",
            "3f2a0000-0000-4000-8000-0000000000c1.png.png",
            "%2e%2e%2fsecret.png"
    })
    void keysAlmostValidNeverReachTheProvider(String key) {
        assertThatThrownBy(() -> service.load(key)).isInstanceOf(StoredFileNotFoundException.class);

        service.delete(key);

        assertThat(storage.calls).isZero();
    }

    @Test
    void unreadableUploadBecomesAStorageExceptionAndStoresNothing() {
        MockMultipartFile unreadable = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}) {
            @Override
            public InputStream getInputStream() throws IOException {
                throw new IOException("client disconnected");
            }
        };

        assertThatThrownBy(() -> service.store(unreadable)).isInstanceOf(StorageException.class);
        assertThat(storage.calls).isZero();
    }

    @Test
    void closesTheUploadStreamEvenWhenTheProviderFails() {
        AtomicBoolean closed = new AtomicBoolean();
        MockMultipartFile upload = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}) {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[]{1}) {
                    @Override
                    public void close() {
                        closed.set(true);
                    }
                };
            }
        };
        FileStorageService failing = new FileStorageService(new UnavailableStorage(), List.of("png"));

        assertThatThrownBy(() -> failing.store(upload)).isInstanceOf(StorageException.class);
        assertThat(closed).isTrue();
    }

    @Test
    void providerFailureIsNeitherHiddenNorTurnedIntoNotFound() {
        FileStorageService failing = new FileStorageService(new UnavailableStorage(), List.of("png"));

        assertThatThrownBy(() -> failing.load(VALID_KEY)).isExactlyInstanceOf(StorageException.class);
        assertThatThrownBy(() -> failing.delete(VALID_KEY)).isExactlyInstanceOf(StorageException.class);
    }

    @Test
    void unexpectedProviderErrorNeverExposesItsMessage() {
        StoragePort leaking = new StoragePort() {
            @Override
            public void store(String key, InputStream content, long size) {
                throw new IllegalArgumentException("bucket secret-bucket");
            }

            @Override
            public Optional<StoredFile> load(String key) {
                throw new IllegalStateException("bucket secret-bucket");
            }

            @Override
            public void delete(String key) {
                throw new IllegalStateException("bucket secret-bucket");
            }
        };
        FileStorageService failing = new FileStorageService(leaking, List.of("png"));
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});

        assertThatThrownBy(() -> failing.store(file))
                .isExactlyInstanceOf(StorageException.class).hasMessageNotContaining("secret-bucket");
        assertThatThrownBy(() -> failing.load(VALID_KEY))
                .isExactlyInstanceOf(StorageException.class).hasMessageNotContaining("secret-bucket");
        assertThatThrownBy(() -> failing.delete(VALID_KEY))
                .isExactlyInstanceOf(StorageException.class).hasMessageNotContaining("secret-bucket");
    }

    private static final class UnavailableStorage implements StoragePort {

        @Override
        public void store(String key, InputStream content, long size) {
            throw new StorageException("down", null);
        }

        @Override
        public Optional<StoredFile> load(String key) {
            throw new StorageException("down", null);
        }

        @Override
        public void delete(String key) {
            throw new StorageException("down", null);
        }
    }

    private static final class InMemoryStorage implements StoragePort {

        private final Map<String, byte[]> files = new HashMap<>();
        private int calls;

        @Override
        public void store(String key, InputStream content, long size) {
            calls++;
            try {
                files.put(key, content.readAllBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Optional<StoredFile> load(String key) {
            calls++;
            return Optional.ofNullable(files.get(key))
                    .map(bytes -> new StoredFile(new ByteArrayInputStream(bytes), bytes.length));
        }

        @Override
        public void delete(String key) {
            calls++;
            files.remove(key);
        }
    }
}
