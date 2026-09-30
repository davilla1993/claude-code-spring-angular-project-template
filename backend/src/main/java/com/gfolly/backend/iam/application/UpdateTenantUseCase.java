package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.UpdateTenantRequest;
import com.gfolly.backend.iam.api.dto.responses.TenantResponse;
import com.gfolly.backend.iam.domain.Tenant;
import com.gfolly.backend.iam.domain.exception.TenantNotFoundException;
import com.gfolly.backend.iam.infrastructure.mapper.TenantMapper;
import com.gfolly.backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.backend.infrastructure.multitenant.TenantContext;
import com.gfolly.backend.shared.util.ErrorMessages;
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
        if (request.currencyCode() != null) tenant.setCurrencyCode(request.currencyCode());
        if (request.country() != null) tenant.setCountry(request.country());
        if (request.address() != null) tenant.setAddress(request.address());
        if (request.phone() != null) tenant.setPhone(request.phone());

        return TenantMapper.toResponse(tenantRepository.save(tenant));
    }
}

