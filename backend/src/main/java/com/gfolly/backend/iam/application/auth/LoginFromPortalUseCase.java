package com.gfolly.quantly_backend.iam.application.auth;

import com.gfolly.quantly_backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.quantly_backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.quantly_backend.iam.api.dto.responses.TenantSelectionResponse;
import com.gfolly.quantly_backend.iam.application.SendVerificationEmailUseCase;
import com.gfolly.quantly_backend.iam.application.dto.AuthSessionResult;
import com.gfolly.quantly_backend.iam.domain.Role;
import com.gfolly.quantly_backend.iam.domain.Tenant;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.EmailNotVerifiedException;
import com.gfolly.quantly_backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.quantly_backend.iam.domain.exception.TenantInactiveException;
import com.gfolly.quantly_backend.iam.infrastructure.repository.TenantRepository;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContextUtils;
import com.gfolly.quantly_backend.reporting.infrastructure.query.SaleReportQueries;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.system.domain.AuditLog;
import com.gfolly.quantly_backend.system.infrastructure.service.AuditLogService;
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
    private final SaleReportQueries saleReportQueries;

    @Transactional
    public AuthSessionResult execute(LoginRequest request) {
        List<User> users = userRepository.findAllByEmailGlobal(request.email());
        if (users.isEmpty()) {
            throw new InvalidCredentialsException(ErrorMessages.LOGIN_NO_DOMAIN_REQUIRED);
        }

        // Le login "portail" (sans sous-domaine) est réservé à OWNER et COMMANDER : on ne sélectionne
        // JAMAIS une ligne employé (CASHIER/MANAGER/ANALYST) même si elle partage cet email et arriverait
        // en tête d'une liste dont l'ordre n'est pas garanti (findAllByEmailGlobal n'a pas d'ORDER BY).
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

        // Si multi-boutique pour OWNER, on renvoie la liste — restreinte aux boutiques dont cet email
        // est réellement OWNER (jamais une boutique où il n'est qu'employé d'un tiers).
        List<User> ownerShops = users.stream().filter(u -> u.getRole() == Role.OWNER).toList();

        if (ownerShops.size() > 1 && firstUser.getRole() == Role.OWNER) {
            var shops = ownerShops.stream()
                    .map(u -> {
                        String tid = userRepository.findRawTenantIdByPublicId(u.getPublicId()).orElse(null);
                        Tenant tenant = tenantRepository.findByPublicId(tid).orElse(null);
                        if (tenant == null) return null;

                        // Switch context to compute CA
                        java.math.BigDecimal ca = TenantContextUtils.callInTenantContext(tid, () ->
                                saleReportQueries.computeCA(
                                        LocalDate.now().atStartOfDay(),
                                        LocalDateTime.now()
                                )
                        );

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

            // On génère quand même un token pour la première boutique pour permettre les appels API suivants
            Tenant firstTenant = tenantRepository.findByPublicId(firstUser.getTenantId()).orElseThrow();
            
            // SÉCURITÉ : On bloque si multishop est OFF et la boutique est inactive.
            // Si multishop est ON, on laisse passer pour voir le portail.
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
