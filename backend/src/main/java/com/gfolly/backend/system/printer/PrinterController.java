package com.gfolly.quantly_backend.system.printer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/printer")
@RequiredArgsConstructor
@Slf4j
public class PrinterController {

    private final PrinterSigningService signingService;

    @PostMapping(value = "/sign", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sign(@RequestBody String message) throws Exception {
        log.debug("Signing request for message: {}", message);
        String signature = signingService.sign(message);
        log.debug("Generated signature: {}", signature);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(signature);
    }
}
