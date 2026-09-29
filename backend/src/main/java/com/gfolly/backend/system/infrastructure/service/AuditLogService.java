package com.gfolly.quantly_backend.system.infrastructure.service;

import com.gfolly.quantly_backend.infrastructure.multitenant.TenantContext;
import com.gfolly.quantly_backend.shared.util.SecurityUtils;
import com.gfolly.quantly_backend.system.domain.AuditLog;
import com.gfolly.quantly_backend.system.infrastructure.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(String action, String entityType, String entityId, String details, AuditLog.ActionStatus status) {
        log(action, entityType, entityId, details, status, null);
    }

    @Async
    public void log(String action, String entityType, String entityId, String details,
                    AuditLog.ActionStatus status, String errorMessage) {
        String userEmail = resolveUserEmail();
        String ipAddress = resolveIpAddress();
        String tenantId  = TenantContext.getCurrentTenant();

        AuditLog entry = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .status(status)
                .errorMessage(errorMessage)
                .actionDate(LocalDateTime.now())
                .build();

        // tenantId may be null for system-level events (e.g. login before tenant resolution)
        if (tenantId != null) {
            entry.setTenantId(tenantId);
        }

        auditLogRepository.save(entry);
    }

    private String resolveUserEmail() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                Object email = attrs.getRequest().getAttribute("currentUserEmail");
                if (email != null) return email.toString();
            }
        } catch (Exception ignored) {}
        return SecurityUtils.getCurrentUserId();
    }

    private String resolveIpAddress() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception ignored) {
            return null;
        }
    }
}
