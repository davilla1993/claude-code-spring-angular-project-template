package com.gfolly.backend.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Retourne le publicId de l'utilisateur connecté depuis le SecurityContext.
     * Retourne "SYSTEM" si aucun utilisateur n'est authentifié.
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isNotAuthenticated(authentication)) {
            return "SYSTEM";
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.userId();
        }
        
        return principal.toString();
    }

    /**
     * Retourne l'email de l'utilisateur connecté depuis le SecurityContext.
     * Retourne "SYSTEM" si aucun utilisateur n'est authentifié.
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isNotAuthenticated(authentication)) {
            return "SYSTEM";
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.email();
        }
        
        return principal.toString();
    }

    public static String getCurrentUserFullName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isNotAuthenticated(authentication)) {
            return "Système";
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.fullName();
        }
        
        return "Système";
    }

    private static boolean isNotAuthenticated(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal());
    }
}


