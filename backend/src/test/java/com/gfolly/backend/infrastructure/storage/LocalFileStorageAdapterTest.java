package com.gfolly.backend.infrastructure.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageAdapterTest extends StoragePortContractTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageAdapter adapter;

    @Override
    protected StoragePort createStorage() {
        adapter = new LocalFileStorageAdapter(tempDir.resolve("uploads").toString());
        return adapter;
    }

    /** Le répertoire de stockage a disparu et un fichier a pris sa place : aucune écriture ni lecture n'y est possible. */
    @Override
    protected StoragePort createUnavailableStorage() throws Exception {
        Path root = tempDir.resolve("unavailable");
        LocalFileStorageAdapter unavailable = new LocalFileStorageAdapter(root.toString());
        Files.delete(root);
        Files.writeString(root, "not a directory");
        return unavailable;
    }

    @Test
    void startupFailsWithAStorageExceptionWhenTheRootCannotBeCreated() throws Exception {
        Path file = Files.writeString(tempDir.resolve("file.txt"), "not a directory");

        assertThatThrownBy(() -> new LocalFileStorageAdapter(file.toString()))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void refusesAKeyWithANulCharacter() {
        String key = "a\0.png";

        assertThat(adapter.load(key)).isEmpty();
        assertThatCode(() -> adapter.delete(key)).doesNotThrowAnyException();
        assertThatThrownBy(() -> adapter.store(key, new ByteArrayInputStream(new byte[]{1}), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refusesAnAbsoluteKeyOutsideRoot() throws Exception {
        Path outside = Files.writeString(tempDir.resolve("secret.txt"), "keep me");

        assertThat(adapter.load(outside.toString())).isEmpty();
        adapter.delete(outside.toString());

        assertThat(outside).exists();
    }

    @Test
    void refusesASiblingDirectorySharingTheRootPrefix() throws Exception {
        Path sibling = Files.createDirectories(tempDir.resolve("uploads-private"));
        Path secret = Files.writeString(sibling.resolve("secret.txt"), "keep me");

        assertThat(adapter.load("../uploads-private/secret.txt")).isEmpty();
        adapter.delete("../uploads-private/secret.txt");

        assertThat(secret).exists();
    }

    @Test
    void neverDeletesOrOverwritesTheRootItself() throws Exception {
        Path root = tempDir.resolve("uploads");

        adapter.delete(".");
        adapter.delete("sub/..");

        assertThat(root).isDirectory();
        assertThatThrownBy(() -> adapter.store(".", new ByteArrayInputStream(new byte[]{1}), 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aDirectoryIsNotAFile() throws Exception {
        Files.createDirectories(tempDir.resolve("uploads/sub"));

        assertThat(adapter.load("sub")).isEmpty();
    }

    @Test
    void neverOverwritesAnExistingFile() throws Exception {
        adapter.store("a.png", new ByteArrayInputStream(new byte[]{1}), 1);

        assertThatThrownBy(() -> adapter.store("a.png", new ByteArrayInputStream(new byte[]{2}), 1))
                .isInstanceOf(StorageException.class);

        assertThat(tempDir.resolve("uploads/a.png")).hasBinaryContent(new byte[]{1});
    }

    @Test
    void deleteFailureIsRaisedAsAStorageException() throws Exception {
        // Un répertoire non vide est impossible à supprimer : c'est une panne, pas un fichier introuvable.
        Files.createDirectories(tempDir.resolve("uploads/sub"));
        Files.writeString(tempDir.resolve("uploads/sub/file.png"), "x");

        assertThatThrownBy(() -> adapter.delete("sub")).isInstanceOf(StorageException.class);
    }

    @Test
    void refusesToReadOutsideRoot() throws Exception {
        Files.writeString(tempDir.resolve("secret.txt"), "keep me");

        assertThat(adapter.load("../secret.txt")).isEmpty();
    }

    @Test
    void refusesToDeleteOutsideRoot() throws Exception {
        Path outside = Files.writeString(tempDir.resolve("secret.txt"), "keep me");

        adapter.delete("../secret.txt");

        assertThat(outside).exists();
    }

    @Test
    void refusesToWriteOutsideRoot() {
        assertThatThrownBy(() -> adapter.store("../secret.txt", new ByteArrayInputStream(new byte[]{1}), 1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(tempDir.resolve("secret.txt")).doesNotExist();
    }

    @Test
    void ignoresBlankKeysAndTheRootItself() {
        assertThat(adapter.load(" ")).isEmpty();
        assertThat(adapter.load(".")).isEmpty();
    }
}
