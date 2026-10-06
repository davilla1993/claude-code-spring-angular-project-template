package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicIdAndDeletedFalse(String publicId);

    /** L'email doit être normalisé avec {@link User#normalizeEmail(String)}. */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /** Inclut les comptes supprimés : l'email reste réservé (contrainte d'unicité). */
    boolean existsByEmail(String email);

    Page<User> findAllByDeletedFalse(Pageable pageable);

    Page<User> findAllByDeletedFalseAndRole(Role role, Pageable pageable);
}
