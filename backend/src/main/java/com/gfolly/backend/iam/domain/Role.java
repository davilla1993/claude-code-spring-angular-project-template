package com.gfolly.backend.iam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

import static com.gfolly.backend.iam.domain.Permission.*;

/**
 * Rôles de l'application Quantly.
 * Chaque rôle est associé à un ensemble de permissions par défaut.
 */
@RequiredArgsConstructor
@Getter
public enum Role {
    /**
     * Super-administrateur du SaaS.
     */
    COMMANDER(EnumSet.of(
            SYSTEM_MANAGE_TENANTS,
            SYSTEM_MANAGE_SUBSCRIPTIONS,
            SYSTEM_VIEW_AUDIT_LOGS
    )),

    /**
     * Propriétaire du supermarché (Hérite de tout dans son tenant).
     */
    OWNER(EnumSet.of(
            USER_CREATE, USER_UPDATE, USER_DELETE, USER_VIEW, TENANT_UPDATE,
            PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE, PRODUCT_VIEW, STOCK_VIEW, STOCK_ADJUST,
            CASHDESK_MANAGE, CASHDESK_OPEN_SESSION, CASHDESK_VIEW_SESSIONS,
            SALE_CREATE, SALE_CANCEL, SALE_VIEW_HISTORY,
            PURCHASE_MANAGE, PURCHASE_VIEW,
            REPORT_VIEW_FINANCIAL, REPORT_VIEW_INVENTORY, REPORT_VIEW_DASHBOARD,
            AUDIT_LOG_VIEW
    )),

    /**
     * Manager (Gère les opérations quotidiennes et utilisateurs hors OWNER).
     */
    MANAGER(EnumSet.of(
            USER_UPDATE, USER_VIEW,
            PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_VIEW, STOCK_VIEW, STOCK_ADJUST,
            CASHDESK_MANAGE, CASHDESK_OPEN_SESSION, CASHDESK_VIEW_SESSIONS,
            SALE_CREATE, SALE_CANCEL, SALE_VIEW_HISTORY,
            PURCHASE_MANAGE, PURCHASE_VIEW,
            REPORT_VIEW_FINANCIAL, REPORT_VIEW_INVENTORY, REPORT_VIEW_DASHBOARD
    )),

    /**
     * Caissier (Ventes et lecture catalogue).
     */
    CASHIER(EnumSet.of(
            PRODUCT_VIEW,
            CASHDESK_OPEN_SESSION, CASHDESK_VIEW_SESSIONS,
            SALE_CREATE, SALE_VIEW_HISTORY,
            REPORT_VIEW_INVENTORY // Pour savoir si un produit est en stock
    )),

    /**
     * Observateur (Consultation rapports).
     */
    ANALYST(EnumSet.of(
            PRODUCT_VIEW, STOCK_VIEW,
            REPORT_VIEW_FINANCIAL, REPORT_VIEW_INVENTORY, REPORT_VIEW_DASHBOARD
    ));

    private final Set<Permission> permissions;
}


