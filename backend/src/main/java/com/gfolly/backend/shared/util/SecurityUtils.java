package com.gfolly.backend.shared.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    public static final String SYSTEM = "SYSTEM";

    private SecurityUtils() {}

    /**
     * Retourne l'email de l'utilisateur connecté, ou "SYSTEM" si aucun utilisateur n'est authentifié.
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.email();
        }
        return SYSTEM;
    }
}
