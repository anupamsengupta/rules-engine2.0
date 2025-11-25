package com.quickysoft.validation.persistence.repository;

import com.quickysoft.validation.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Rule entities.
 * All queries are tenant-aware.
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {
    
    /**
     * Finds all rules for a given rule set.
     */
    @Query("SELECT r FROM RuleEntity r " +
           "WHERE r.ruleSet.id = :ruleSetId " +
           "AND r.enabled = true " +
           "ORDER BY r.priority ASC, r.ruleCode ASC")
    List<RuleEntity> findByRuleSetIdAndEnabledTrue(@Param("ruleSetId") UUID ruleSetId);
    
    /**
     * Finds all rules for a given rule set (including disabled).
     */
    @Query("SELECT r FROM RuleEntity r " +
           "WHERE r.ruleSet.id = :ruleSetId " +
           "ORDER BY r.priority ASC, r.ruleCode ASC")
    List<RuleEntity> findByRuleSetId(@Param("ruleSetId") UUID ruleSetId);
    
    /**
     * Finds a rule by tenant, rule set code, and rule code.
     */
    @Query("SELECT r FROM RuleEntity r " +
           "JOIN r.ruleSet rs " +
           "WHERE r.tenantId = :tenantId " +
           "AND rs.code = :ruleSetCode " +
           "AND r.ruleCode = :ruleCode")
    List<RuleEntity> findByTenantIdAndRuleSetCodeAndRuleCode(
            @Param("tenantId") String tenantId,
            @Param("ruleSetCode") String ruleSetCode,
            @Param("ruleCode") String ruleCode
    );
    
    /**
     * Finds a rule by tenant and rule code (across all rule sets).
     */
    @Query("SELECT r FROM RuleEntity r " +
           "WHERE r.tenantId = :tenantId " +
           "AND r.ruleCode = :ruleCode")
    List<RuleEntity> findByTenantIdAndRuleCode(
            @Param("tenantId") String tenantId,
            @Param("ruleCode") String ruleCode
    );
    
    /**
     * Finds a rule by ID and tenant (for security).
     */
    @Query("SELECT r FROM RuleEntity r " +
           "WHERE r.id = :id " +
           "AND r.tenantId = :tenantId")
    Optional<RuleEntity> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") String tenantId);
}

