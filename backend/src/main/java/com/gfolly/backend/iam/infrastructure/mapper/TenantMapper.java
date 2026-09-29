package com.gfolly.quantly_backend.iam.infrastructure.mapper;

import com.gfolly.quantly_backend.iam.api.dto.responses.TenantResponse;
import com.gfolly.quantly_backend.iam.api.dto.responses.TenantSelectionResponse;
import com.gfolly.quantly_backend.iam.domain.Tenant;

public class TenantMapper {

    public static TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getPublicId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getPlan(),
                tenant.getActive(),
                tenant.getTvaRate(),
                tenant.getBanknotes(),
                tenant.getCoins(),
                tenant.getCurrencyCode(),
                tenant.getCountry(),
                tenant.getAddress(),
                tenant.getPhone(),
                tenant.getLogoUrl(),
                tenant.getCreatedAt()
        );
    }

    public static TenantSelectionResponse toSelectionResponse(Tenant tenant) {
        return new TenantSelectionResponse(
                tenant.getPublicId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getLogoUrl(),
                null,
                tenant.canAccess()
        );
    }

    private TenantMapper() {}
}
