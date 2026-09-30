package com.gfolly.backend.system.infrastructure.specification;

import com.gfolly.backend.system.domain.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> searchAuditLogs(
            String userEmail,
            String entityType,
            AuditLog.ActionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted records
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

            // Filter by user email (partial match)
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("userEmail")),
                        "%" + userEmail.toLowerCase() + "%"
                ));
            }

            // Filter by entity type (exact match)
            if (entityType != null && !entityType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by start date (greater than or equal)
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("actionDate"), startDate));
            }

            // Filter by end date (less than or equal)
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("actionDate"), endDate));
            }

            // Order by action date descending
            query.orderBy(criteriaBuilder.desc(root.get("actionDate")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}


