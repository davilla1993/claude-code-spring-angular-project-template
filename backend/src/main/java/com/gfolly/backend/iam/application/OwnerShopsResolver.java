package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Résout, pour un email donné, les boutiques dont il est réellement OWNER (ligne User + Tenant).
 * Centralise une résolution auparavant dupliquée dans GetMyShopsUseCase,
 * GetOwnerGlobalDashboardStatsUseCase, GetOwnerShopsUseCase et LoginUseCase.switchShop.
 */
@Service
@RequiredArgsConstructor
public class OwnerShopsResolver {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public record OwnerShop(User user, Tenant tenant) {}

    public List<OwnerShop> resolve(String email) {
        return userRepository.findAllOwnerShopsByEmailGlobal(email).stream()
                .map(u -> {
                    String tid = userRepository.findRawTenantIdByPublicId(u.getPublicId()).orElse(null);
                    Tenant tenant = tid != null ? tenantRepository.findByPublicId(tid).orElse(null) : null;
                    return tenant != null ? new OwnerShop(u, tenant) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
