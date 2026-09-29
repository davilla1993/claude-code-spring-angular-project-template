package com.gfolly.quantly_backend.system.domain;

import com.gfolly.quantly_backend.shared.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_email", columnList = "userEmail"),
        @Index(name = "idx_action_date", columnList = "actionDate"),
        @Index(name = "idx_entity_type", columnList = "entityType"),
        @Index(name = "idx_action_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends TenantAwareEntity {

    @Column(name = "userEmail", length = 255)
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(length = 100)
    private String entityId;

    @Column(length = 2000)
    private String details;

    @Column(length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime actionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    @Column(length = 1000)
    private String errorMessage;

    public enum ActionStatus {
        SUCCESS,
        FAILED
    }
}

