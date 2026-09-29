package com.gfolly.quantly_backend.infrastructure.multitenant;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache In-Memory avec Caffeine.
 * Active le support multi-tenant via des clés isolées.
 */
@Configuration
@EnableCaching
public class MultiTenantCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configuration par défaut pour tous les caches
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES) // TTL par défaut
                .recordStats());
        
        return cacheManager;
    }
}
