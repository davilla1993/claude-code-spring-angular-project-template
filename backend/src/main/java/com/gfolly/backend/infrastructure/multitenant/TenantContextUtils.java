package com.gfolly.backend.infrastructure.multitenant;

import java.util.function.Supplier;

/**
 * Utility class for managing multi-tenancy context execution.
 */
public class TenantContextUtils {

    /**
     * Executes a supplier within a specific tenant context and clears it afterwards.
     */
    public static <T> T callInTenantContext(String tenantId, Supplier<T> supplier) {
        String originalTenant = TenantContext.getCurrentTenant();
        TenantContext.setCurrentTenant(tenantId);
        try {
            return supplier.get();
        } finally {
            if (originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    /**
     * Executes a runnable within a specific tenant context and clears it afterwards.
     */
    public static void runInTenantContext(String tenantId, Runnable runnable) {
        String originalTenant = TenantContext.getCurrentTenant();
        TenantContext.setCurrentTenant(tenantId);
        try {
            runnable.run();
        } finally {
            if (originalTenant != null) {
                TenantContext.setCurrentTenant(originalTenant);
            } else {
                TenantContext.clear();
            }
        }
    }

    private TenantContextUtils() {}
}

