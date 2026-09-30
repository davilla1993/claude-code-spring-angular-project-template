package com.gfolly.backend.infrastructure.config;

import com.gfolly.backend.infrastructure.multitenant.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    @Value("${app.uploads-dir:./uploads}")
    private String uploadsDir;

    @Value("${app.storage.provider:local}")
    private String storageProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                // Les endpoints auth et config n'ont pas encore de contexte tenant
                .excludePathPatterns("/api/auth/**", "/api/config");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Le mapping statique /uploads/** n'est utile que pour le stockage local.
        // Avec MinIO, les images sont accédées directement via des URLs presigned.
        if ("local".equals(storageProvider)) {
            String absolutePath = Paths.get(uploadsDir).toAbsolutePath().toUri().toString();
            if (!absolutePath.endsWith("/")) {
                absolutePath += "/";
            }
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(absolutePath);
        }
    }
}


