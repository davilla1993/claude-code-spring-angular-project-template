package com.gfolly.quantly_backend.iam.api;

import com.gfolly.quantly_backend.iam.api.dto.requests.UpdateTenantRequest;
import com.gfolly.quantly_backend.iam.api.dto.responses.TenantResponse;
import com.gfolly.quantly_backend.iam.application.UpdateTenantUseCase;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.exception.TenantNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.mapper.TenantMapper;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.infrastructure.storage.StoragePort;
import com.gfolly.quantly_backend.shared.api.ApiResponse;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantRepository tenantRepository;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final StoragePort storagePort;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Cacheable(
        cacheNames = "tenants", 
        key = "T(com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext).getCurrentTenant()"
    )
    public ResponseEntity<ApiResponse<TenantResponse>> getMyTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        return tenantRepository.findByPublicId(tenantId)
                .map(tenant -> ResponseEntity.ok(ApiResponse.success(TenantMapper.toResponse(tenant))))
                .orElseThrow(() -> new TenantNotFoundException(ErrorMessages.TENANT_NOT_FOUND));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('tenant:update')")
    @CacheEvict(
        cacheNames = "tenants",
        key = "T(com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext).getCurrentTenant()"
    )
    public ResponseEntity<ApiResponse<TenantResponse>> updateMyTenant(@Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(updateTenantUseCase.execute(request)));
    }

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('tenant:update')")
    @CacheEvict(
        cacheNames = "tenants",
        key = "T(com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext).getCurrentTenant()"
    )
    public ResponseEntity<ApiResponse<TenantResponse>> uploadLogo(@RequestParam("file") MultipartFile file) {
        String tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findByPublicId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(ErrorMessages.TENANT_NOT_FOUND));

        if (tenant.getLogoUrl() != null) {
            storagePort.delete(tenant.getLogoUrl());
        }

        String logoPath = storagePort.store(file);
        tenant.setLogoUrl(logoPath);
        return ResponseEntity.ok(ApiResponse.success(TenantMapper.toResponse(tenantRepository.save(tenant))));
    }

    @DeleteMapping("/me/logo")
    @PreAuthorize("hasAuthority('tenant:update')")
    @CacheEvict(
        cacheNames = "tenants",
        key = "T(com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext).getCurrentTenant()"
    )
    public ResponseEntity<ApiResponse<TenantResponse>> deleteLogo() {
        String tenantId = TenantContext.getCurrentTenant();
        Tenant tenant = tenantRepository.findByPublicId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(ErrorMessages.TENANT_NOT_FOUND));

        if (tenant.getLogoUrl() != null) {
            storagePort.delete(tenant.getLogoUrl());
            tenant.setLogoUrl(null);
            tenantRepository.save(tenant);
        }
        return ResponseEntity.ok(ApiResponse.success(TenantMapper.toResponse(tenant)));
    }
}

