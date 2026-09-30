package com.gfolly.backend.iam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Permission {
    // --- SYSTEM ---
    SYSTEM_MANAGE_TENANTS("system:manage:tenants", "Gérer les établissements (SaaS)"),
    SYSTEM_MANAGE_SUBSCRIPTIONS("system:manage:subscriptions", "Gérer les abonnements"),
    SYSTEM_VIEW_AUDIT_LOGS("system:view:audit", "Voir les logs d'audit globaux"),

    // --- IAM ---
    USER_CREATE("user:create", "Créer des utilisateurs"),
    USER_UPDATE("user:update", "Modifier des utilisateurs"),
    USER_DELETE("user:delete", "Supprimer/Désactiver des utilisateurs"),
    USER_VIEW("user:view", "Voir les utilisateurs"),
    TENANT_UPDATE("tenant:update", "Modifier les infos de l'établissement"),

    // --- INVENTORY ---
    PRODUCT_CREATE("product:create", "Créer des produits"),
    PRODUCT_UPDATE("product:update", "Modifier des produits"),
    PRODUCT_DELETE("product:delete", "Supprimer des produits"),
    PRODUCT_VIEW("product:view", "Voir le catalogue produits"),
    STOCK_VIEW("stock:view", "Voir les inventaires et mouvements de stock"),
    STOCK_ADJUST("stock:adjust", "Ajuster les stocks manuellement"),

    // --- CASHDESK ---
    CASHDESK_MANAGE("cashdesk:manage", "Gérer les terminaux et la session journalière"),
    CASHDESK_OPEN_SESSION("cashdesk:session:open", "Ouvrir et fermer sa session caissier"),
    CASHDESK_VIEW_SESSIONS("cashdesk:session:view", "Consulter les sessions de caisse"),

    // --- SALES / POS ---
    SALE_CREATE("sale:create", "Effectuer des ventes"),
    SALE_CANCEL("sale:cancel", "Annuler des ventes"),
    SALE_VIEW_HISTORY("sale:view:history", "Voir l'historique des ventes"),

    // --- PURCHASES ---
    PURCHASE_MANAGE("purchase:manage", "Gérer les achats et fournisseurs"),
    PURCHASE_VIEW("purchase:view", "Voir les achats et fournisseurs"),

    // --- REPORTING ---
    REPORT_VIEW_FINANCIAL("report:view:financial", "Voir les rapports financiers"),
    REPORT_VIEW_INVENTORY("report:view:inventory", "Voir l'état des stocks"),
    REPORT_VIEW_DASHBOARD("report:view:dashboard", "Accéder au tableau de bord"),

    // --- AUDIT ---
    AUDIT_LOG_VIEW("audit:log:view", "Voir les journaux d'audit du tenant");

    private final String code;
    private final String description;
}


