package com.gfolly.backend.infrastructure.config;

import com.gfolly.backend.shared.util.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fournit l'auteur courant pour @CreatedBy / @LastModifiedBy : l'email de l'utilisateur authentifié,
 * ou "SYSTEM" hors contexte utilisateur.
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(SecurityUtils.getCurrentUserEmail());
    }
}
