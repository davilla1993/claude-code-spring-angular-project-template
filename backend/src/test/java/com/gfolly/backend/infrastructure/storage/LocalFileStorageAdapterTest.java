package com.gfolly.backend.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageAdapterTest {

    @TempDir
    Path tempDir;

    private Path root;
    private LocalFileStorageAdapter storage;

    @BeforeEach
    void setUp() {
        root = tempDir.resolve("uploads");
        storage = new LocalFileStorageAdapter(root.toString(), List.of("png", "pdf"));
    }

    @Test
    void storesAllowedFileUnderGeneratedName() throws Exception {
        String key = storage.store(new MockMultipartFile("file", "Photo.PNG", "image/png", new byte[]{1, 2, 3}));

        assertThat(key).matches("[0-9a-f-]{36}\\.png");
        assertThat(Files.readAllBytes(root.resolve(key))).containsExactly(1, 2, 3);
    }

    @Test
    void rejectsDisallowedOrMissingExtension() {
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "x.html", "text/html", new byte[]{1})))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "noext", "image/png", new byte[]{1})))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> storage.store(new MockMultipartFile("file", "x.png", "image/png", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deletesStoredFile() {
        String key = storage.store(new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1}));

        storage.delete(key);

        assertThat(root.resolve(key)).doesNotExist();
    }

    @Test
    void refusesToDeleteOutsideRoot() throws Exception {
        Path outside = Files.writeString(tempDir.resolve("secret.txt"), "keep me");

        storage.delete("../secret.txt");

        assertThat(outside).exists();
    }
}
