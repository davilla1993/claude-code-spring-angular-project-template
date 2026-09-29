package com.gfolly.quantly_backend.iam.application;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;

import com.gfolly.quantly_backend.iam.api.dto.requests.CreateShopRequest;
import com.gfolly.quantly_backend.iam.application.auth.AuthTokenService;
import com.gfolly.quantly_backend.iam.application.dto.AuthSessionResult;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.SlugAlreadyExistsException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContextUtils;
import com.gfolly.quantly_backend.catalog.application.TenantInitializationService;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.shared.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterAdditionalShopUseCase {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantInitializationService tenantInitializationService;
    private final AuthTokenService authTokenService;
    private final CreateOwnerService createOwnerService;

    @Transactional
    public AuthSessionResult execute(CreateShopRequest request) {
        String currentEmail = SecurityUtils.getCurrentUserEmail();

        User currentUser = userRepository.findByEmailGlobal(currentEmail)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.USER_NOT_FOUND_IN_CONTEXT));

        if (!Boolean.TRUE.equals(currentUser.getShopCreationEnabled())) {
            throw new AccessDeniedException("La création de nouvelles boutiques n'est pas activée pour votre compte. Contactez le support pour l'activer.");
        }

        validateSlugAvailability(request.slug());

        Tenant tenant = new Tenant();
        tenant.setName(request.businessName());
        tenant.setSlug(request.slug());
        tenant.setCountry(request.country());
        tenant.setAddress(request.address());
        tenant.setPhone(request.phone());
        final Tenant savedTenant = tenantRepository.save(tenant);

        return TenantContextUtils.callInTenantContext(savedTenant.getPublicId(), () -> {
            // Initialisation de la boutique (Doit être dans un contexte isolé)
            tenantInitializationService.initialize(savedTenant.getPublicId());

            // Création du propriétaire dans le nouveau tenant via une nouvelle transaction
            User savedUser = createOwnerService.createOwnerInNewTenant(currentUser, savedTenant.getPublicId());

            // La nouvelle boutique doit être immédiatement accessible via le switcher multi-boutique :
            // on aligne multishop sur toutes les boutiques de cet email (ancienne(s) + nouvelle).
            userRepository.setMultishopByEmailGlobal(currentUser.getEmail(), true);
            savedUser.setMultishop(true);

            // shopCreationEnabled est un droit à usage unique (un shop = une facturation) : on le
            // redésactive immédiatement après création pour que COMMANDER doive le réactiver
            // explicitement avant la prochaine boutique, sans dépendre d'un rate-limit générique.
            userRepository.setShopCreationEnabledByEmailGlobal(currentUser.getEmail(), false);
            savedUser.setShopCreationEnabled(false);

            return authTokenService.issueTokens(savedUser, savedTenant);
        });
    }

    private void validateSlugAvailability(String slug) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new SlugAlreadyExistsException(ErrorMessages.subdomainAlreadyExists(slug));
        }
    }
}
