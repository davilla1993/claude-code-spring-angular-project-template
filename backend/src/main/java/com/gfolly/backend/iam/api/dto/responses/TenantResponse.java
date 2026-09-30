package com.gfolly.backend.iam.api.dto.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TenantResponse(
        String publicId,
        String name,
        String slug,
        String plan,
        Boolean active,
        BigDecimal tvaRate,
        String banknotes,
        String coins,
        String currencyCode,
        String country,
        String address,
        String phone,
        String logoUrl,
        LocalDateTime createdAt
) {}

