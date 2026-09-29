package com.gfolly.quantly_backend.iam.application.auth;

import com.gfolly.quantly_backend.iam.api.dto.responses.OwnerGlobalDashboardResponse;
import com.gfolly.quantly_backend.iam.application.InternalShopStatsService;
import com.gfolly.quantly_backend.iam.application.OwnerShopsResolver;
import com.gfolly.quantly_backend.iam.application.dto.ShopInternalStats;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class GetOwnerGlobalDashboardStatsUseCase {

    private final OwnerShopsResolver ownerShopsResolver;
    private final InternalShopStatsService internalShopStatsService;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    private record ShopResult(Tenant tenant, ShopInternalStats stats) {}

    @Transactional(readOnly = true)
    public OwnerGlobalDashboardResponse execute(String email, LocalDate from, LocalDate to) {
        List<OwnerShopsResolver.OwnerShop> shops = ownerShopsResolver.resolve(email);

        // Partie coûteuse (CA + alertes stock par boutique) : en parallèle, une boutique n'attend pas l'autre.
        // Chaque tâche fixe elle-même son TenantContext sur son propre thread (ThreadLocal isolé par thread).
        List<CompletableFuture<ShopResult>> futures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(
                        () -> {
                            var stats = TenantContextUtils.callInTenantContext(shop.tenant().getPublicId(),
                                    () -> internalShopStatsService.getStatsForShop(shop.tenant().getPublicId(), from, to));
                            return new ShopResult(shop.tenant(), stats);
                        },
                        taskExecutor))
                .toList();

        BigDecimal totalCA = BigDecimal.ZERO;
        List<OwnerGlobalDashboardResponse.ShopStats> shopStatsList = new ArrayList<>();
        List<OwnerGlobalDashboardResponse.StockAlert> stockAlerts = new ArrayList<>();

        // Agrégation séquentielle des résultats une fois toutes les tâches terminées — pas de mutation concurrente.
        for (CompletableFuture<ShopResult> future : futures) {
            ShopResult result = future.join();
            Tenant tenant = result.tenant();
            ShopInternalStats stats = result.stats();

            totalCA = totalCA.add(stats.ca());
            shopStatsList.add(new OwnerGlobalDashboardResponse.ShopStats(
                    tenant.getPublicId(),
                    tenant.getName(),
                    tenant.getSlug(),
                    tenant.getLogoUrl(),
                    stats.ca(),
                    stats.transactions(),
                    tenant.canAccess()
            ));

            for (OwnerGlobalDashboardResponse.StockAlert alert : stats.alerts()) {
                stockAlerts.add(new OwnerGlobalDashboardResponse.StockAlert(
                        tenant.getName(),
                        alert.productName(),
                        alert.currentStock(),
                        alert.threshold(),
                        alert.unit()
                ));
            }
        }

        return new OwnerGlobalDashboardResponse(totalCA, shops.size(), shopStatsList, stockAlerts);
    }
}
