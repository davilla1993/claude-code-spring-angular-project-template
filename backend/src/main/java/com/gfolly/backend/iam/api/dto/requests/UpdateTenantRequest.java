package com.gfolly.quantly_backend.iam.api.dto.requests;

import java.math.BigDecimal;

public record UpdateTenantRequest(
    String name,
    BigDecimal tvaRate,
    String banknotes,
    String coins,
    String currencyCode,
    String country,
    String address,
    String phone
) {}
