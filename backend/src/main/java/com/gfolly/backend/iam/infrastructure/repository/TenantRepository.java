package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByPublicId(String publicId);
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT t.slug FROM Tenant t WHERE t.publicId = :publicId")
    Optional<String> findSlugByPublicId(String publicId);

    @Query("SELECT t FROM Tenant t WHERE t.slug != :systemSlug ORDER BY t.createdAt DESC")
    org.springframework.data.domain.Page<Tenant> findAllExcludingSystem(
            @org.springframework.data.repository.query.Param("systemSlug") String systemSlug,
            org.springframework.data.domain.Pageable pageable);

    @Query(value = "SELECT * FROM tenants t " +
           "WHERE t.slug <> :systemSlug " +
           "AND (:search IS NULL OR :search = '' " +
           "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR t.public_id IN (SELECT u.tenant_id FROM users u WHERE u.role = 'OWNER' AND LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) AND u.deleted = false)) " +
           "ORDER BY t.created_at DESC", nativeQuery = true)
    org.springframework.data.domain.Page<Tenant> searchExcludingSystem(
            @org.springframework.data.repository.query.Param("systemSlug") String systemSlug,
            @org.springframework.data.repository.query.Param("search") String search,
            org.springframework.data.domain.Pageable pageable);

    /** Suspend tous les tenants actifs dont la date d'expiration est dépassée. */
    @Modifying
    @Query("UPDATE Tenant t SET t.active = false WHERE t.active = true AND t.expiresAt IS NOT NULL AND t.expiresAt < :now")
    int suspendExpired(@org.springframework.data.repository.query.Param("now") LocalDateTime now);
}


