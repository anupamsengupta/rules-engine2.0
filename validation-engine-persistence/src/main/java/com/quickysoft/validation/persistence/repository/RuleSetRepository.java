package com.quickysoft.validation.persistence.repository;

import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RuleSet entities.
 * All queries are tenant-aware.
 */
@Repository
public interface RuleSetRepository extends JpaRepository<RuleSetEntity, UUID> {
    
    /**
     * Finds a rule set by tenant, code, and version.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.code = :code " +
           "AND rs.version = :version")
    Optional<RuleSetEntity> findByTenantIdAndCodeAndVersion(
            @Param("tenantId") String tenantId,
            @Param("code") String code,
            @Param("version") String version
    );
    
    /**
     * Finds an active rule set by tenant, code, and version.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.code = :code " +
           "AND rs.version = :version " +
           "AND rs.active = true")
    Optional<RuleSetEntity> findActiveByTenantIdAndCodeAndVersion(
            @Param("tenantId") String tenantId,
            @Param("code") String code,
            @Param("version") String version
    );
    
    /**
     * Finds all rule sets for a tenant and code (all versions).
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.code = :code " +
           "ORDER BY rs.version DESC")
    List<RuleSetEntity> findByTenantIdAndCode(
            @Param("tenantId") String tenantId,
            @Param("code") String code
    );
    
    /**
     * Finds all active rule sets for a tenant and code.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.code = :code " +
           "AND rs.active = true " +
           "ORDER BY rs.version DESC")
    List<RuleSetEntity> findActiveByTenantIdAndCode(
            @Param("tenantId") String tenantId,
            @Param("code") String code
    );
    
    /**
     * Finds the latest active version of a rule set for a tenant and code.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.code = :code " +
           "AND rs.active = true " +
           "ORDER BY rs.version DESC")
    List<RuleSetEntity> findAllActiveByTenantIdAndCodeOrderByVersionDesc(
            @Param("tenantId") String tenantId,
            @Param("code") String code
    );
    
    /**
     * Finds the latest active version of a rule set for a tenant and code.
     * Returns the first result from the ordered list.
     */
    default Optional<RuleSetEntity> findLatestActiveByTenantIdAndCode(String tenantId, String code) {
        List<RuleSetEntity> results = findAllActiveByTenantIdAndCodeOrderByVersionDesc(tenantId, code);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * Finds all rule sets for a tenant with pagination.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "ORDER BY rs.code ASC, rs.version DESC")
    Page<RuleSetEntity> findByTenantId(@Param("tenantId") String tenantId, Pageable pageable);
    
    /**
     * Checks if a rule set exists for the given tenant, code, and version.
     */
    boolean existsByTenantIdAndCodeAndVersion(String tenantId, String code, String version);
}

