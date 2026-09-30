package com.gfolly.backend.system.application;

import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.system.api.dto.responses.AuditLogResponse;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.repository.AuditLogRepository;
import com.gfolly.backend.system.infrastructure.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GetAuditLogsUseCase {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> execute(
            String userEmail,
            String entityType,
            AuditLog.ActionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<AuditLog> spec = AuditLogSpecification.searchAuditLogs(
                userEmail, entityType, status, startDate, endDate);

        Page<AuditLogResponse> page = auditLogRepository.findAll(spec, pageable)
                .map(this::toResponse);

        return PageResponse.from(page);
    }

    private AuditLogResponse toResponse(AuditLog log) {
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
}

