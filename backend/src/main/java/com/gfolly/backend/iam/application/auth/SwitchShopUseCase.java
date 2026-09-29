package com.gfolly.quantly_backend.iam.application.auth;

import com.gfolly.quantly_backend.iam.application.OwnerShopsResolver;
import com.gfolly.quantly_backend.iam.application.dto.AuthSessionResult;
import com.gfolly.quantly_backend.iam.domain.Role;
import com.gfolly.quantly_backend.iam.domain.exception.TenantInactiveException;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SwitchShopUseCase {

    private final OwnerShopsResolver ownerShopsResolver;
    private final AuthTokenService authTokenService;

    @Transactional
    public AuthSessionResult execute(String email, String shopPublicId) {
        OwnerShopsResolver.OwnerShop shop = ownerShopsResolver.resolve(email).stream()
                .filter(s -> s.tenant().getPublicId().equals(shopPublicId))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Vous n'avez pas accès à cette boutique."));

        // Défense en profondeur : OwnerShopsResolver ne renvoie déjà que des lignes OWNER,
        // mais on garde le contrôle explicite ici — c'est la règle métier de cette action.
        if (shop.user().getRole() != Role.OWNER) {
            throw new AccessDeniedException("Seul le propriétaire peut basculer entre les boutiques.");
        }

        if (!Boolean.TRUE.equals(shop.user().getMultishop())) {
            throw new AccessDeniedException("L'accès multi-boutiques est désactivé pour votre compte.");
        }

        if (!shop.tenant().canAccess()) {
            throw new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
        }

        return authTokenService.issueTokens(shop.user(), shop.tenant());
    }
}
