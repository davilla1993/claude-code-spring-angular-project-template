package com.gfolly.backend.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sert les fichiers stockés, quel que soit le fournisseur. Sous /api : ils bénéficient donc de
 * l'authentification (le cookie access_token est limité au chemin /api).
 */
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadsController {

    private final FileStorageService fileStorage;

    /** Le type de contenu vient de l'extension de la clé, déjà contrôlée par {@link FileStorageService}. */
    @GetMapping("/{key}")
    public ResponseEntity<InputStreamResource> read(@PathVariable String key) {
        StoredFile file = fileStorage.load(key);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .contentLength(file.size())
                // Explicite, sinon la protection anti-RFD de Spring nomme les PDF « f.txt ».
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(key).build().toString())
                .body(new InputStreamResource(file.content()));
    }
}
