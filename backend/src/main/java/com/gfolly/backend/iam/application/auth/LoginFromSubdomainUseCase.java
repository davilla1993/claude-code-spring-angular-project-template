package com.gfolly.backend.iam.application.auth;

import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import com.gfolly.backend.iam.domain.Tenant;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.domain.exception.TenantInactiveException;
import com.gfolly.backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.infrastructure.multitenant.TenantContextUtils;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginFromSubdomainUseCase {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthSessionResult execute(LoginRequest request) {
        Tenant tenant = tenantRepository.findBySlug(request.subdomain())
                .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.LOGIN_DOMAIN_REQUIRED));

        if (!tenant.canAccess()) {
            throw new TenantInactiveException(ErrorMessages.TENANT_INACTIVE);
        }

        return TenantContextUtils.callInTenantContext(tenant.getPublicId(), () -> {
            // Cherche d'abord par username (employés), sinon par email (owner)
            User user = userRepository.findByUsernameAndTenantId(request.email(), tenant.getPublicId())
                    .or(() -> userRepository.findByEmailAndTenantId(request.email(), tenant.getPublicId()))
                    .orElseThrow(() -> new InvalidCredentialsException(ErrorMessages.LOGIN_DOMAIN_REQUIRED));

            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new InvalidCredentialsException(ErrorMessages.ACCOUNT_DISABLED);
            }

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new InvalidCredentialsException(ErrorMessages.LOGIN_DOMAIN_REQUIRED);
            }

            AuthSessionResult result = authTokenService.issueTokens(user, tenant);
            auditLogService.log("LOGIN_SUCCESS", "USER", user.getPublicId(),
                    "Subdomain login: " + request.subdomain(), AuditLog.ActionStatus.SUCCESS);
            return result;
        });
    }
}

