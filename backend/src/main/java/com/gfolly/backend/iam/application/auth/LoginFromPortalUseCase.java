package com.gfolly.backend.iam.application.auth;

import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.backend.iam.api.dto.responses.TenantSelectionResponse;
import com.gfolly.backend.iam.application.SendVerificationEmailUseCase;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.Tenant;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailNotVerifiedException;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.domain.exception.TenantInactiveException;
import com.gfolly.backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.infrastructure.multitenant.TenantContextUtils;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginFromPortalUseCase {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final AuditLogService auditLogService;
    private final AuthTokenService authTokenService;

    @Transactional
    public AuthSessionResult execute(LoginRequest request) {
        List<User> users = userRepository.findAllByEmailGlobal(request.email());
        if (users.isEmpty()) {
            throw new InvalidCredentialsException(ErrorMessages.LOGIN_NO_DOMAIN_REQUIRED);
        }

        User firstUser = users.stream()
                .filter(u -> u.getRole() == Role.OWNER || u.getRole() == Role.COMMANDER)
                .findFirst()
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.LOGIN_NO_DOMAIN_REQUIRED));

        if (!Boolean.TRUE.equals(firstUser.getActive())) {
            throw new InvalidCredentialsException(ErrorMessages.ACCOUNT_DISABLED);
        }

        if (!passwordEncoder.matches(request.password(), firstUser.getPasswordHash())) {
            throw new InvalidCredentialsException(ErrorMessages.LOGIN_NO_DOMAIN_REQUIRED);
        }

        if (!Boolean.TRUE.equals(firstUser.getEmailVerified())) {
            sendVerificationEmailUseCase.execute(firstUser.getPublicId(), firstUser.getEmail(), firstUser.getFirstName());
            throw new EmailNotVerifiedException(ErrorMessages.EMAIL_NOT_VERIFIED, firstUser.getPublicId());
        }

        List<User> ownerShops = users.stream().filter(u -> u.getRole() == Role.OWNER).toList();

        if (ownerShops.size() > 1 && firstUser.getRole() == Role.OWNER) {
            var shops = ownerShops.stream()
                    .map(u -> {
                        String tid = userRepository.findRawTenantIdByPublicId(u.getPublicId()).orElse(null);
                        Tenant tenant = tenantRepository.findByPublicId(tid).orElse(null);
                        if (tenant == null) return null;

                        // On remplace l'appel à saleReportQueries par ZERO pour le template
                        java.math.BigDecimal ca = java.math.BigDecimal.ZERO;

                        return new TenantSelectionResponse(
                                tenant.getPublicId(),
                                tenant.getName(),
                                tenant.getSlug(),
                                tenant.getLogoUrl(),
                                ca,
                                tenant.canAccess()
                        );
                    })
                    .filter(Objects::nonNull)
                    .toList();

            Tenant firstTenant = tenantRepository.findByPublicId(firstUser.getTenantId()).orElseThrow();

            if (!Boolean.TRUE.equals(firstUser.getMultishop()) && !firstTenant.canAccess()) {
                throw new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
            }

            AuthSessionResult result = TenantContextUtils.callInTenantContext(firstTenant.getPublicId(), () ->
                    authTokenService.issueTokens(firstUser, firstTenant)
            );

            return new AuthSessionResult(
                    result.tokens(),
                    new AuthResponse(result.response().user(), shops)
            );
        }

        String tenantId = userRepository.findRawTenantIdByPublicId(firstUser.getPublicId())
                .orElseThrow(() -> {
                    log.error("[LOGIN] rawTenantId introuvable pour userId={}", firstUser.getPublicId());
                    return new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
                });

        Tenant tenant = tenantRepository.findByPublicId(tenantId)
                .orElseThrow(() -> {
                    log.error("[LOGIN] Tenant NOT FOUND pour tenantId={}", tenantId);
                    return new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
                });

        if (!tenant.canAccess()) {
            throw new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
        }

        AuthSessionResult result = TenantContextUtils.callInTenantContext(tenant.getPublicId(), () ->
                authTokenService.issueTokens(firstUser, tenant)
        );
        auditLogService.log("LOGIN_SUCCESS", "USER", firstUser.getPublicId(),
                "Portal login", AuditLog.ActionStatus.SUCCESS);
        return result;
    }
}
