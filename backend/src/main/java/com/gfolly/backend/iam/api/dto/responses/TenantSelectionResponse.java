package com.gfolly.quantly_backend.iam.api.dto.responses;

import java.math.BigDecimal;

public record TenantSelectionResponse(
        String publicId,
        String name,
        String slug,
        String logoUrl,
        BigDecimal dailyCA,
        boolean active
) {}
