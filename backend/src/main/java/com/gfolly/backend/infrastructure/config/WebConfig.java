package com.gfolly.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadsLocation;

    public WebConfig(@Value("${app.storage.uploads-dir}") String uploadsDir) {
        String uri = Path.of(uploadsDir).toAbsolutePath().normalize().toUri().toString();
        this.uploadsLocation = uri.endsWith("/") ? uri : uri + "/";
    }

    /**
     * Fichiers déposés via StoragePort, servis sous /api : ils bénéficient donc de l'authentification
     * (le cookie access_token est limité au chemin /api).
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadsLocation);
    }
}
