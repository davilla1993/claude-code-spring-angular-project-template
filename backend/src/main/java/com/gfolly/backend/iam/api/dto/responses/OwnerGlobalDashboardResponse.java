package com.gfolly.quantly_backend.iam.api.dto.responses;

import java.math.BigDecimal;
import java.util.List;

public record OwnerGlobalDashboardResponse(
        BigDecimal totalCA,
        int totalShops,
        List<ShopStats> shops,
        List<StockAlert> stockAlerts
) {
    public record ShopStats(
            String publicId,
            String name,
            String slug,
            String logoUrl,
            BigDecimal ca,
            long transactions,
            boolean active
    ) {}

    public record StockAlert(
            String shopName,
            String productName,
            BigDecimal currentStock,
            BigDecimal threshold,
            String unit
    ) {}
}
