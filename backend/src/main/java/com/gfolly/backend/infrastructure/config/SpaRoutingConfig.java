package com.gfolly.quantly_backend.infrastructure.config;

import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Configuration pour servir l'application Angular (SPA)
 * Toutes les routes non-API sont redirigées vers index.html
 * pour permettre le routing côté client Angular
 */
@Configuration
public class SpaRoutingConfig implements WebMvcConfigurer {

    private final String uploadsDir;

    public SpaRoutingConfig(@Value("${app.uploads-dir:./uploads}") String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les images uploadées avec isolation par tenant
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadsDir + "/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        // resourcePath est de la forme "tenant-id/filename.ext"
                        String currentTenant = TenantContext.getCurrentTenant();
                        
                        // Si pas de tenant en contexte ou si le chemin ne commence pas par le tenant courant,
                        // on refuse l'accès en retournant null (ce qui provoquera un 404)
                        if (currentTenant == null || !resourcePath.startsWith(currentTenant + "/")) {
                            return null;
                        }
                        
                        return super.getResource(resourcePath, location);
                    }
                });

        // Servir les fichiers statiques Angular et gérer le routing SPA
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        // Si on demande la racine, renvoyer index.html
                        if (resourcePath.isEmpty() || resourcePath.equals("/")) {
                            Resource index = new ClassPathResource("/static/index.html");
                            return index.exists() ? index : null;
                        }

                        Resource requestedResource = location.createRelative(resourcePath);

                        // Si la ressource existe (fichier statique direct) et n'est pas un répertoire, la renvoyer
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            // On vérifie que ce n'est pas un répertoire
                            if (!resourcePath.endsWith("/")) {
                                return requestedResource;
                            }
                        }

                        // Sinon, si ce n'est pas une route API, renvoyer index.html pour le routage Angular
                        if (!resourcePath.startsWith("api/")) {
                            Resource index = new ClassPathResource("/static/index.html");
                            return index.exists() ? index : null;
                        }

                        return null;
                    }
                });
    }
}

