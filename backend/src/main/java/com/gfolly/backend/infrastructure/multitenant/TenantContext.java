package com.gfolly.quantly_backend.infrastructure.multitenant;

import lombok.extern.slf4j.Slf4j;

/**
 * TenantContext - Gestion du contexte tenant par thread
 *
 * Utilise ThreadLocal pour stocker le publicId du tenant courant.
 * Chaque requête HTTP aura son propre context isolé.
 *
 * Le context est défini par TenantInterceptor au début de chaque requête
 * et nettoyé à la fin de la requête.
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Définit le tenant courant pour ce thread
     * 
     * @param tenantId Le publicId du tenant
     */
    public static void setCurrentTenant(String tenantId) {
        if (tenantId != null) {
            log.debug("Setting tenant context to: {}", tenantId);
            currentTenant.set(tenantId);
        }
    }

    /**
     * Obtient le tenant courant de ce thread
     * 
     * @return Le publicId du tenant, ou null si non défini
     */
    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Nettoie le context du tenant pour ce thread
     * IMPORTANT: Doit être appelé à la fin de chaque requête
     */
    public static void clear() {
        String tenant = currentTenant.get();
        if (tenant != null) {
            log.debug("Clearing tenant context: {}", tenant);
        }
        currentTenant.remove();
    }

    /**
     * Vérifie si un tenant est défini pour ce thread
     * 
     * @return true si un tenant est défini, false sinon
     */
    public static boolean isSet() {
        return currentTenant.get() != null;
    }

    /**
     * Obtient le tenant courant ou un tenant par défaut
     * 
     * @param defaultTenantId Le tenant par défaut si aucun tenant n'est défini
     * @return Le tenant courant ou le tenant par défaut
     */
    public static String getCurrentTenantOrDefault(String defaultTenantId) {
        String tenant = currentTenant.get();
        return tenant != null ? tenant : defaultTenantId;
    }
}

