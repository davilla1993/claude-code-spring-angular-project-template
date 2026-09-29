package com.gfolly.quantly_backend.iam.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShopInternalStats(
    BigDecimal ca,
    long transactions,
    List<com.gfolly.quantly_backend.iam.api.dto.responses.OwnerGlobalDashboardResponse.StockAlert> alerts
) {}
