package com.gfolly.quantly_backend.system.api.dto.responses;

import com.gfolly.quantly_backend.system.domain.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        String id,
        String userEmail,
        String action,
        String entityType,
        String entityId,
        String details,
        String ipAddress,
        AuditLog.ActionStatus status,
        String errorMessage,
        LocalDateTime actionDate
) {}
