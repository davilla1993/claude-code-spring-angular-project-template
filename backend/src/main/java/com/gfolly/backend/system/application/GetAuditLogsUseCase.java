package com.gfolly.backend.system.application;

import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.system.api.dto.responses.AuditLogResponse;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.mapper.response.AuditLogResponseMapper;
import com.gfolly.backend.system.infrastructure.repository.AuditLogRepository;
import com.gfolly.backend.system.infrastructure.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

        var spec = AuditLogSpecification.searchAuditLogs(userEmail, entityType, status, startDate, endDate);
        return PageResponse.from(auditLogRepository.findAll(spec, pageable).map(AuditLogResponseMapper::toResponse));
    }
}
