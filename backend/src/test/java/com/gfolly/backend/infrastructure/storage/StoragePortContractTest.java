package com.gfolly.backend.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Comportement attendu de tout fournisseur de stockage : chaque adaptateur a sa classe qui hérite de ce test. */
abstract class StoragePortContractTest {

    private static final String KEY = "3f2a0000-0000-4000-8000-0000000000c1.png";
    private static final byte[] CONTENT = {1, 2, 3};

    private StoragePort storage;

    protected abstract StoragePort createStorage() throws Exception;

    /** Le même fournisseur, en panne (service injoignable, disque indisponible...). */
    protected abstract StoragePort createUnavailableStorage() throws Exception;

    @BeforeEach
    void createPort() throws Exception {
        storage = createStorage();
    }

    @Test
    void returnsWhatWasStored() throws Exception {
        storage.store(KEY, new ByteArrayInputStream(CONTENT), CONTENT.length);

        StoredFile file = storage.load(KEY).orElseThrow();

        assertThat(file.size()).isEqualTo(CONTENT.length);
        try (InputStream content = file.content()) {
            assertThat(content.readAllBytes()).containsExactly(CONTENT);
        }
    }

    @Test
    void loadReturnsEmptyWhenTheFileDoesNotExist() {
        assertThat(storage.load(KEY)).isEmpty();
    }

    @Test
    void deleteRemovesTheFile() throws Exception {
        storage.store(KEY, new ByteArrayInputStream(CONTENT), CONTENT.length);

        storage.delete(KEY);

        assertThat(storage.load(KEY)).isEmpty();
    }

    @Test
    void deleteHasNoEffectWhenTheFileDoesNotExist() {
        assertThatCode(() -> storage.delete(KEY)).doesNotThrowAnyException();
    }

    @Test
    void storeRaisesStorageExceptionWhenTheProviderIsUnavailable() throws Exception {
        StoragePort unavailable = createUnavailableStorage();

        assertThatThrownBy(() -> unavailable.store(KEY, new ByteArrayInputStream(CONTENT), CONTENT.length))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void loadRaisesStorageExceptionWhenTheProviderIsUnavailable() throws Exception {
        StoragePort unavailable = createUnavailableStorage();

        // Une panne ne doit pas se confondre avec un fichier absent (500 et non 404 côté API).
        assertThatThrownBy(() -> unavailable.load(KEY)).isInstanceOf(StorageException.class);
    }

    @Test
    void storeNeverOverwritesAnExistingFile() throws Exception {
        storage.store(KEY, new ByteArrayInputStream(CONTENT), CONTENT.length);

        assertThatThrownBy(() -> storage.store(KEY, new ByteArrayInputStream(new byte[]{9}), 1))
                .isInstanceOf(StorageException.class);

        try (InputStream content = storage.load(KEY).orElseThrow().content()) {
            assertThat(content.readAllBytes()).containsExactly(CONTENT);
        }
    }

    @Test
    void storeRaisesStorageExceptionWhenTheContentCannotBeRead() {
        assertThatThrownBy(() -> storage.store(KEY, new FailingAfterTwoBytes(), CONTENT.length))
                .isInstanceOf(StorageException.class);
    }

    @Test
    void failedStoreLeavesNoTruncatedFile() throws Exception {
        assertThatThrownBy(() -> storage.store(KEY, new FailingAfterTwoBytes(), CONTENT.length))
                .isInstanceOf(StorageException.class);

        Optional<StoredFile> left = storage.load(KEY);
        if (left.isPresent()) {
            left.get().content().close();
        }
        assertThat(left).isEmpty();
    }

    private static final class FailingAfterTwoBytes extends InputStream {

        private int remaining = 2;

        @Override
        public int read() throws IOException {
            if (remaining-- > 0) {
                return 1;
            }
            throw new IOException("connection lost");
        }
    }
}
