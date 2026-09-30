package com.gfolly.backend.infrastructure.multitenant;

import org.springframework.context.annotation.Configuration;

/**
 * Stratégie : DISCRIMINATOR (Single Schema avec @TenantId Hibernate).
 * Le CurrentTenantIdentifierResolver est enregistré via application.yml :
 *   spring.jpa.properties.hibernate.tenant_identifier_resolver
 * Hibernate l'instancie directement (TenantContext étant statique, pas besoin de DI).
 */
@Configuration
public class MultiTenantJpaConfig {

}


