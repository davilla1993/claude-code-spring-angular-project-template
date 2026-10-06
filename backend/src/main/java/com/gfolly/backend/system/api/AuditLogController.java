package com.gfolly.backend.system.api;

import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.system.api.dto.responses.AuditLogResponse;
import com.gfolly.backend.system.application.GetAuditLogsUseCase;
import com.gfolly.backend.system.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final GetAuditLogsUseCase getAuditLogsUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('audit:log:view')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditLog.ActionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "actionDate", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                getAuditLogsUseCase.execute(userEmail, entityType, status, from, to, pageable)));
    }
}
