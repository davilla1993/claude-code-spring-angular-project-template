package com.gfolly.backend.infrastructure.multitenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TenantInterceptor — Nettoyage du contexte tenant après chaque requête.
 *
 * Le TenantContext est alimenté par JwtAuthenticationFilter (via les claims JWT)
 * avant l'arrivée dans ce intercepteur.
 *
 * Rôle de cet intercepteur :
 * - preHandle  : vérifie que le contexte est présent (log uniquement)
 * - afterCompletion : nettoie le ThreadLocal pour éviter les fuites mémoire
 */
@Component
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.debug("TenantInterceptor.preHandle — tenant: {}", TenantContext.getCurrentTenant());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}


