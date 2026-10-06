package com.gfolly.backend.system.infrastructure.mapper.response;

import com.gfolly.backend.system.api.dto.responses.AuditLogResponse;
import com.gfolly.backend.system.domain.AuditLog;

public final class AuditLogResponseMapper {

    public static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getPublicId(),
                log.getUserEmail(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getDetails(),
                log.getIpAddress(),
                log.getStatus(),
                log.getErrorMessage(),
                log.getActionDate()
        );
    }

    private AuditLogResponseMapper() {}
}
