package com.gfolly.backend.infrastructure.multitenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import com.gfolly.backend.infrastructure.multitenant.TenantContext;

/**
 * CurrentTenantIdentifierResolverImpl - Résout le tenant identifier pour
 * Hibernate
 *
 * Hibernate appelle cette méthode à chaque fois qu'il a besoin de savoir
 * quel est le tenant courant pour effectuer une opération de base de données.
 *
 * Cette implémentation récupère le tenant depuis TenantContext (ThreadLocal)
 * qui a été défini par TenantInterceptor au début de la requête.
 */
@Component
@Slf4j
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    /**
     * Résout le tenant identifier courant
     *
     * @return Le publicId du tenant, ou "DEFAULT" par défaut
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();

        if (tenant != null) {
            log.trace("Resolved current tenant identifier: {}", tenant);
            return tenant;
        }

        // Fallback (pour les entités master ou opérations hors contexte)
        log.trace("No tenant context, using DEFAULT identifier");
        return "DEFAULT";
    }

    /**
     * Indique si Hibernate doit valider les sessions existantes
     * lors du changement de tenant
     *
     * @return true pour valider, false sinon
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        // Valider les sessions pour s'assurer qu'elles correspondent au bon tenant
        return true;
    }
}


