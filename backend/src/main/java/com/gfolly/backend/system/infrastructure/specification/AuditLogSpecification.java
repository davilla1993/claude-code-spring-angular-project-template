package com.gfolly.backend.system.infrastructure.specification;

import com.gfolly.backend.system.domain.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Filtres de recherche des logs d'audit. Le tri est porté par le Pageable.
 */
public final class AuditLogSpecification {

    public static Specification<AuditLog> searchAuditLogs(
            String userEmail,
            String entityType,
            AuditLog.ActionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (userEmail != null && !userEmail.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("userEmail")),
                        "%" + escapeLike(userEmail.trim().toLowerCase(Locale.ROOT)) + "%", '\\'));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("actionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("actionDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Échappe les jokers LIKE pour que la saisie utilisateur soit traitée littéralement. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private AuditLogSpecification() {}
}
