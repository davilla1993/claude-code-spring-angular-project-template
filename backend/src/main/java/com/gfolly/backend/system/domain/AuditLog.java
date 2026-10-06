package com.gfolly.backend.system.domain;

import com.gfolly.backend.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user_email", columnList = "user_email"),
        @Index(name = "idx_audit_action_date", columnList = "action_date"),
        @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
        @Index(name = "idx_audit_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

    @Column(name = "user_email")
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(length = 2000)
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public enum ActionStatus {
        SUCCESS,
        FAILED
    }
}
