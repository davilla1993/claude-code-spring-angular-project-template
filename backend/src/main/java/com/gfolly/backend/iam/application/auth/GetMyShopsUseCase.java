package com.gfolly.quantly_backend.iam.application.auth;

import com.gfolly.quantly_backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.quantly_backend.iam.api.dto.responses.TenantSelectionResponse;
import com.gfolly.quantly_backend.iam.application.InternalShopStatsService;
import com.gfolly.quantly_backend.iam.application.OwnerShopsResolver;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class GetMyShopsUseCase {

    private final OwnerShopsResolver ownerShopsResolver;
    private final InternalShopStatsService internalShopStatsService;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    @Transactional
    public AuthResponse execute(String email) {
        List<OwnerShopsResolver.OwnerShop> shops = ownerShopsResolver.resolve(email);

        // Partie coûteuse (CA par boutique) : en parallèle, une boutique n'attend pas l'autre.
        // Chaque tâche fixe elle-même son TenantContext sur son propre thread (ThreadLocal isolé par thread).
        List<CompletableFuture<TenantSelectionResponse>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> TenantContextUtils.callInTenantContext(shop.tenant().getPublicId(), () -> {
                            var stats = internalShopStatsService.getStatsForShop(shop.tenant().getPublicId(), LocalDate.now(), LocalDate.now());
                            return new TenantSelectionResponse(
                                    shop.tenant().getPublicId(),
                                    shop.tenant().getName(),
                                    shop.tenant().getSlug(),
                                    shop.tenant().getLogoUrl(),
                                    stats.ca(),
                                    shop.tenant().canAccess()
                            );
                        }),
                        taskExecutor))
                .toList();

        List<TenantSelectionResponse> result = futures.stream().map(CompletableFuture::join).toList();

        return new AuthResponse(result);
    }
}
