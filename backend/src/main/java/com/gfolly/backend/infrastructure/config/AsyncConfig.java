package com.gfolly.backend.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Active @Async (envoi d'emails). L'exécuteur est celui auto-configuré par Spring Boot
 * (propriétés spring.task.execution.* / spring.threads.virtual.enabled).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
