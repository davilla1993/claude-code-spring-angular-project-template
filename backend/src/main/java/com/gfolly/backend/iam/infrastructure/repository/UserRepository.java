package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicIdAndDeletedFalse(String publicId);

    /** L'email doit être normalisé avec {@link User#normalizeEmail(String)}. */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /** Inclut les comptes supprimés : l'email reste réservé (contrainte d'unicité). */
    boolean existsByEmail(String email);

    @Query("select u.currentSessionId from User u where u.publicId = :publicId and u.deleted = false and u.active = true")
    Optional<String> findCurrentSessionId(@Param("publicId") String publicId);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    @Query("select u from User u where u.deleted = false and :role member of u.roles")
    Page<User> findAllNotDeletedHavingRole(@Param("role") Role role, Pageable pageable);
}
