package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.api.dto.requests.UpdateTenantRequest;
import com.gfolly.quantly_backend.iam.api.dto.responses.TenantResponse;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.exception.TenantNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.mapper.TenantMapper;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateTenantUseCase {

    private final TenantRepository tenantRepository;

    @Transactional
    public TenantResponse execute(UpdateTenantRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findByPublicId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(ErrorMessages.TENANT_NOT_FOUND));

        if (request.name() != null) tenant.setName(request.name());
        if (request.tvaRate() != null) tenant.setTvaRate(request.tvaRate());
        if (request.banknotes() != null) tenant.setBanknotes(request.banknotes());
        if (request.coins() != null) tenant.setCoins(request.coins());
        if (request.currencyCode() != null) tenant.setCurrencyCode(request.currencyCode());
        if (request.country() != null) tenant.setCountry(request.country());
        if (request.address() != null) tenant.setAddress(request.address());
        if (request.phone() != null) tenant.setPhone(request.phone());

        return TenantMapper.toResponse(tenantRepository.save(tenant));
    }
}
