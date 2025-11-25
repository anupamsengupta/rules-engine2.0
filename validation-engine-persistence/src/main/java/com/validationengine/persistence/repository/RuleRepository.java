package com.validationengine.persistence.repository;

import com.validationengine.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Rule entities.
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, Long> {
    
    /**
     * Finds all rules for a given rule set.
     */
    @Query("SELECT r FROM RuleEntity r " +
           "WHERE r.ruleSet.id = :ruleSetId " +
           "AND r.active = true " +
           "ORDER BY r.priority ASC, r.ruleCode ASC")
    List<RuleEntity> findByRuleSetIdAndActiveTrue(@Param("ruleSetId") Long ruleSetId);
    
    /**
     * Finds a rule by tenant, rule set code, and rule code.
     */
    @Query("SELECT r FROM RuleEntity r " +
           "JOIN r.ruleSet rs " +
           "WHERE r.tenantId = :tenantId " +
           "AND rs.ruleSetCode = :ruleSetCode " +
           "AND r.ruleCode = :ruleCode")
    List<RuleEntity> findByTenantIdAndRuleSetCodeAndRuleCode(
            @Param("tenantId") String tenantId,
            @Param("ruleSetCode") String ruleSetCode,
            @Param("ruleCode") String ruleCode
    );
}

