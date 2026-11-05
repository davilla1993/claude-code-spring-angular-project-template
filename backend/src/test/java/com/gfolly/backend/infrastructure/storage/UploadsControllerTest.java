package com.gfolly.backend.infrastructure.storage;

import com.gfolly.backend.shared.api.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Réponses de l'API de lecture avec un faux fournisseur : elles ne dépendent d'aucun stockage réel. */
class UploadsControllerTest {

    private static final List<String> EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp", "pdf", "zzz");
    private static final byte[] CONTENT = {1, 2, 3};

    private static MockMvc mvc(StoragePort storage) {
        return MockMvcBuilders
                .standaloneSetup(new UploadsController(new FileStorageService(storage, EXTENSIONS)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static StoragePort serving(InputStream stream) {
        return new StoragePort() {
            @Override
            public void store(String key, InputStream content, long size) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<StoredFile> load(String key) {
                return Optional.of(new StoredFile(stream, CONTENT.length));
            }

            @Override
            public void delete(String key) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static String keyWith(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    @ParameterizedTest
    @CsvSource({
            "jpg, image/jpeg",
            "jpeg, image/jpeg",
            "png, image/png",
            "gif, image/gif",
            "webp, image/webp",
            "pdf, application/pdf",
            "zzz, application/octet-stream"
    })
    void contentTypeComesFromTheKeyExtension(String extension, String expected) throws Exception {
        mvc(serving(new ByteArrayInputStream(CONTENT)))
                .perform(get("/api/uploads/" + keyWith(extension)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(expected));
    }

    @Test
    void aPdfKeepsItsOwnNameInsteadOfTheAntiRfdFallback() throws Exception {
        String key = keyWith("pdf");

        mvc(serving(new ByteArrayInputStream(CONTENT)))
                .perform(get("/api/uploads/" + key))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", "inline; filename=\"" + key + "\""));
    }

    @Test
    void closesTheProviderStreamAfterTheResponse() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        InputStream stream = new ByteArrayInputStream(CONTENT) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };

        mvc(serving(stream)).perform(get("/api/uploads/" + keyWith("png"))).andExpect(status().isOk());

        assertThat(closed).isTrue();
    }

    @Test
    void rangeHeaderDoesNotBreakTheResponse() throws Exception {
        for (String range : List.of("bytes=0-1", "bytes=999-1000", "not-a-range")) {
            mvc(serving(new ByteArrayInputStream(CONTENT)))
                    .perform(get("/api/uploads/" + keyWith("png")).header("Range", range))
                    .andExpect(status().isOk())
                    .andExpect(content().bytes(CONTENT));
        }
    }

    @Test
    void providerFailureReturnsAnErrorWithoutProviderDetails() throws Exception {
        StoragePort unavailable = new StoragePort() {
            @Override
            public void store(String key, InputStream content, long size) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<StoredFile> load(String key) {
                throw new StorageException("bucket-secret unreachable", new IOException("10.0.0.5:9000"));
            }

            @Override
            public void delete(String key) {
                throw new UnsupportedOperationException();
            }
        };

        mvc(unavailable).perform(get("/api/uploads/" + keyWith("png")))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("bucket-secret"))))
                .andExpect(content().string(not(containsString("10.0.0.5"))));
    }

    @Test
    void invalidAndMissingKeysAreIndistinguishable() throws Exception {
        StoragePort empty = new StoragePort() {
            @Override
            public void store(String key, InputStream content, long size) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<StoredFile> load(String key) {
                return Optional.empty();
            }

            @Override
            public void delete(String key) {
                throw new UnsupportedOperationException();
            }
        };
        MockMvc mvc = mvc(empty);

        String missing = mvc.perform(get("/api/uploads/" + keyWith("png")))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();
        String invalid = mvc.perform(get("/api/uploads/secret.png"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        assertThat(invalid).isEqualTo(missing);
    }
}
