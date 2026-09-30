package com.gfolly.backend.infrastructure.config;

import com.gfolly.backend.shared.util.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Fournit l'identité de l'auditeur courant pour @CreatedBy / @LastModifiedBy.
 * Lit le userId (publicId) depuis le SecurityContext alimenté par JwtAuthenticationFilter.
 */
@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal userPrincipal) {
            return Optional.ofNullable(userPrincipal.email());
        }

        if ("anonymousUser".equals(principal)) {
            return Optional.of("SYSTEM");
        }

        return Optional.of(principal.toString());
    }
}


