package com.quickysoft.validationengine.persistence.repository;

import com.quickysoft.validationengine.persistence.entity.RuleSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RuleSet entities.
 */
@Repository
public interface RuleSetRepository extends JpaRepository<RuleSetEntity, Long> {
    
    /**
     * Finds a rule set by tenant, code, and version.
     */
    @Query("SELECT rs FROM RuleSetEntity rs " +
           "WHERE rs.tenantId = :tenantId " +
           "AND rs.ruleSetCode = :ruleSetCode " +
           "AND rs.version = :version")
    Optional<RuleSetEntity> findByTenantIdAndRuleSetCodeAndVersion(
            @Param("tenantId") String tenantId,
            @Param("ruleSetCode") String ruleSetCode,
            @Param("version") String version
    );
    
    /**
     * Checks if a rule set exists for the given tenant, code, and version.
     */
    boolean existsByTenantIdAndRuleSetCodeAndVersion(
            String tenantId,
            String ruleSetCode,
            String version
    );
}

