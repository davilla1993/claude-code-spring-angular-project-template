package com.gfolly.backend.iam.infrastructure.repository;

import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPublicId(String publicId);

    /** Charge l'utilisateur ET sa caisse assignée en une seule requête (évite LazyInit + EntityNotFound). */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cashRegister WHERE u.publicId = :publicId AND u.deleted = false")
    Optional<User> findByPublicIdWithCashRegister(@Param("publicId") String publicId);

    // Requête native pour contourner le filtre @TenantId lors de la connexion,
    // où le contexte tenant n'est pas encore établi.
    @Query(value = "SELECT * FROM users WHERE email = :email AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailGlobal(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE email = :email AND deleted = false", nativeQuery = true)
    List<User> findAllByEmailGlobal(@Param("email") String email);

    /**
     * Comme findAllByEmailGlobal, mais restreint aux boutiques dont cet email est OWNER.
     * À utiliser pour toute vue "mes boutiques" d'un owner — évite d'exposer/agréger les données
     * d'un tenant où cet email n'est qu'employé (CASHIER/MANAGER/ANALYST) d'une boutique tierce.
     */
    @Query(value = "SELECT * FROM users WHERE email = :email AND role = 'OWNER' AND deleted = false", nativeQuery = true)
    List<User> findAllOwnerShopsByEmailGlobal(@Param("email") String email);

    // Recherche par publicId sans filtre tenant (vérification email, etc.)
    @Query(value = "SELECT * FROM users WHERE public_id = :publicId AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<User> findByPublicIdGlobal(@Param("publicId") String publicId);

    // Lit le tenant_id brut sans passer par le mapping @TenantId de Hibernate
    @Query(value = "SELECT tenant_id FROM users WHERE public_id = :publicId AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<String> findRawTenantIdByPublicId(@Param("publicId") String publicId);

    // Updates natifs : contournent le filtre tenant_id = 'DEFAULT' qu'Hibernate injecte
    // dans les WHERE de UPDATE sur les entités @TenantId sans contexte tenant.
    @Modifying
    @Query(value = "UPDATE users SET email_verified = true WHERE public_id = :publicId", nativeQuery = true)
    void markEmailVerified(@Param("publicId") String publicId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET password_hash = :hash WHERE public_id = :publicId", nativeQuery = true)
    void updatePasswordHash(@Param("publicId") String publicId, @Param("hash") String hash);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET password_hash = :hash, first_login = :firstLogin WHERE public_id = :publicId", nativeQuery = true)
    void updatePasswordHashAndFirstLogin(@Param("publicId") String publicId, @Param("hash") String hash, @Param("firstLogin") boolean firstLogin);

    /**
     * Corrige le tenant_id après insertion.
     * flushAutomatically : force le flush (INSERT) avant l'UPDATE → les entités pending sont persistées.
     * clearAutomatically : vide le cache après l'UPDATE → Hibernate ne réécrasera pas le tenant_id au commit.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE users SET tenant_id = :tenantId WHERE public_id = :publicId", nativeQuery = true)
    void fixTenantId(@Param("publicId") String publicId, @Param("tenantId") String tenantId);

    // Connexion depuis un sous-domaine : email unique dans le tenant
    @Query(value = "SELECT * FROM users WHERE email = :email AND tenant_id = :tenantId AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") String tenantId);

    // Utilisé dans le contexte tenant déjà défini (ex: vérifier doublons à l'invitation)
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Connexion depuis un sous-domaine : par username
    @Query(value = "SELECT * FROM users WHERE username = :username AND tenant_id = :tenantId AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<User> findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") String tenantId);

    /** Pagination sans fetch (utilisé quand cashRegister n'est pas nécessaire). */
    Page<User> findAllByDeletedFalse(Pageable pageable);
    Page<User> findAllByDeletedFalseAndRole(Role role, Pageable pageable);

    /** Pagination avec caisse fetchée — pour les endpoints qui exposent cashRegisterId. */
    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.cashRegister WHERE u.deleted = false",
           countQuery = "SELECT COUNT(u) FROM User u WHERE u.deleted = false")
    Page<User> findAllByDeletedFalseWithCashRegister(Pageable pageable);

    /** Tous les utilisateurs du tenant courant ayant une caisse attribuée, avec la caisse fetchée en une seule requête. */
    @Query("SELECT u FROM User u JOIN FETCH u.cashRegister WHERE u.deleted = false AND u.cashRegister IS NOT NULL")
    List<User> findAllByDeletedFalseAndCashRegisterIsNotNull();

    /** Tous les utilisateurs du tenant courant assignés à une caisse donnée (par publicId). */
    List<User> findAllByDeletedFalseAndCashRegister_PublicId(String registerId);

    /** Tous les users d'un tenant — requête native pour contourner le filtre @TenantId (usage COMMANDER). */
    @Query(value = "SELECT * FROM users WHERE tenant_id = :tenantId AND deleted = false ORDER BY role, last_name", nativeQuery = true)
    List<User> findAllByTenantIdNative(@Param("tenantId") String tenantId);

    /** Toggle actif/inactif sans filtre tenant (usage COMMANDER). */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE users SET active = :active WHERE public_id = :publicId AND deleted = false", nativeQuery = true)
    void setActiveByPublicId(@Param("publicId") String publicId, @Param("active") boolean active);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE users SET multishop = :multishop WHERE public_id = :publicId AND deleted = false", nativeQuery = true)
    void setMultishopByPublicId(@Param("publicId") String publicId, @Param("multishop") boolean multishop);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET password_hash = :hash, first_login = false WHERE email = :email AND role IN ('OWNER', 'COMMANDER')", nativeQuery = true)
    void updatePasswordHashGlobal(@Param("email") String email, @Param("hash") String hash);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE users SET multishop = :multishop WHERE email = :email AND deleted = false", nativeQuery = true)
    void setMultishopByEmailGlobal(@Param("email") String email, @Param("multishop") boolean multishop);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE users SET shop_creation_enabled = :enabled WHERE email = :email AND deleted = false", nativeQuery = true)
    void setShopCreationEnabledByEmailGlobal(@Param("email") String email, @Param("enabled") boolean enabled);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET first_name = :firstName, last_name = :lastName WHERE email = :email AND role = 'OWNER'", nativeQuery = true)
    void updateProfileGlobal(@Param("email") String email, @Param("firstName") String firstName, @Param("lastName") String lastName);

    @Query(value = "SELECT first_name, last_name, email, multishop, active, shop_creation_enabled, " +
           "(SELECT COUNT(*) FROM users u2 WHERE u2.email = u.email AND u2.role = 'OWNER' AND u2.deleted = false) as shop_count " +
           "FROM users u " +
           "WHERE role = 'OWNER' AND deleted = false " +
           "AND (:search IS NULL OR :search = '' OR LOWER(email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "GROUP BY email, first_name, last_name, multishop, active, shop_creation_enabled " +
           "ORDER BY first_name ASC",
           countQuery = "SELECT COUNT(DISTINCT email) FROM users WHERE role = 'OWNER' AND deleted = false AND (:search IS NULL OR :search = '' OR LOWER(email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(first_name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', :search, '%')))",
           nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> findUniqueOwnersGlobal(@Param("search") String search, org.springframework.data.domain.Pageable pageable);
}


