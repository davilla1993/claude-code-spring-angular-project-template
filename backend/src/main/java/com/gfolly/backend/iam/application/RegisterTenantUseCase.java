package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.api.dto.requests.RegisterTenantRequest;
import com.gfolly.quantly_backend.iam.application.auth.AuthTokenService;
import com.gfolly.quantly_backend.iam.application.dto.AuthSessionResult;
import com.gfolly.quantly_backend.iam.domain.Role;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.EmailAlreadyExistsException;
import com.gfolly.quantly_backend.iam.domain.exception.SlugAlreadyExistsException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContextUtils;
import com.gfolly.quantly_backend.catalog.application.TenantInitializationService;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.shared.util.NormalizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterTenantUseCase {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final TenantInitializationService tenantInitializationService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    @Transactional
    public AuthSessionResult execute(RegisterTenantRequest request) {
        validateSlugAvailability(request.slug());
        validateEmailAvailability(request.email());

        Tenant tenant = createTenant(request);

        return TenantContextUtils.callInTenantContext(tenant.getPublicId(), () -> {
            // Délégation de l'initialisation (Seeding)
            tenantInitializationService.initialize(tenant.getPublicId());

            User owner = createOwner(request, tenant.getPublicId());

            // Envoi du code de vérification par email (asynchrone)
            sendVerificationEmailUseCase.execute(owner.getPublicId(), owner.getEmail(), owner.getFirstName());

            return authTokenService.issueTokens(owner, tenant);
        });
    }

    private void validateSlugAvailability(String slug) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new SlugAlreadyExistsException(ErrorMessages.subdomainAlreadyExists(slug));
        }
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.findByEmailGlobal(email).isPresent()) {
            throw new EmailAlreadyExistsException(ErrorMessages.emailAlreadyExists(email));
        }
    }

    private Tenant createTenant(RegisterTenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.businessName());
        tenant.setSlug(request.slug());
        tenant.setCountry(request.country());
        tenant.setAddress(request.address());
        tenant.setPhone(request.phone());
        tenant.setActive(true);
        return tenantRepository.save(tenant);
    }

    private User createOwner(RegisterTenantRequest request, String tenantId) {
        User owner = new User();
        owner.setEmail(request.email());
        owner.setPasswordHash(passwordEncoder.encode(request.password()));
        owner.setFirstName(NormalizationUtils.formatFirstName(request.firstName()));
        owner.setLastName(NormalizationUtils.formatLastName(request.lastName()));
        owner.setRole(Role.OWNER);
        owner.setEmailVerified(true);
        owner.setFirstLogin(true);
        User saved = userRepository.save(owner);
        userRepository.fixTenantId(saved.getPublicId(), tenantId);
        return saved;
    }
}
