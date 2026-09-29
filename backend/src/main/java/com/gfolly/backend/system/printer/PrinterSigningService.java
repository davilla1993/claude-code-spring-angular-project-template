package com.gfolly.quantly_backend.system.printer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

@Service
public class PrinterSigningService {

    @Value("${app.printer.keystore.password:changeit}")
    private String keystorePassword;

    @Value("${app.printer.key.alias:qz}")
    private String keyAlias;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (InputStream is = new ClassPathResource("qz-signing.p12").getInputStream()) {
            keystore.load(is, keystorePassword.toCharArray());
        }
        privateKey = (PrivateKey) keystore.getKey(keyAlias, keystorePassword.toCharArray());
    }

    public String sign(String request) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(request.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }
}
