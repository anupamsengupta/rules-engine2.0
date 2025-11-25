package com.quickysoft.validation.core.model;

import java.util.List;
import java.util.UUID;

/**
 * Result of evaluating a complete rule set.
 * 
 * Contains the rule set identification, overall status, and individual rule results.
 * The overall status is determined based on the rule results and their severities.
 */
public record RuleSetResult(
        UUID ruleSetId,
        String tenantId,
        String ruleSetCode,
        String ruleSetVersion,
        RuleSetStatus overallStatus,
        List<RuleResult> ruleResults
) {
    
    public RuleSetResult {
        if (ruleSetId == null) {
            throw new IllegalArgumentException("ruleSetId cannot be null");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (ruleSetCode == null || ruleSetCode.isBlank()) {
            throw new IllegalArgumentException("ruleSetCode cannot be null or blank");
        }
        if (ruleSetVersion == null || ruleSetVersion.isBlank()) {
            throw new IllegalArgumentException("ruleSetVersion cannot be null or blank");
        }
        if (overallStatus == null) {
            throw new IllegalArgumentException("overallStatus cannot be null");
        }
        if (ruleResults == null) {
            ruleResults = List.of();
        }
    }
    
    /**
     * Creates a builder for RuleSetResult.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for RuleSetResult.
     */
    public static class Builder {
        private UUID ruleSetId;
        private String tenantId;
        private String ruleSetCode;
        private String ruleSetVersion;
        private RuleSetStatus overallStatus;
        private List<RuleResult> ruleResults = List.of();
        
        public Builder ruleSetId(UUID ruleSetId) {
            this.ruleSetId = ruleSetId;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder ruleSetCode(String ruleSetCode) {
            this.ruleSetCode = ruleSetCode;
            return this;
        }
        
        public Builder ruleSetVersion(String ruleSetVersion) {
            this.ruleSetVersion = ruleSetVersion;
            return this;
        }
        
        public Builder overallStatus(RuleSetStatus overallStatus) {
            this.overallStatus = overallStatus;
            return this;
        }
        
        public Builder ruleResults(List<RuleResult> ruleResults) {
            this.ruleResults = ruleResults != null ? ruleResults : List.of();
            return this;
        }
        
        public RuleSetResult build() {
            return new RuleSetResult(
                    ruleSetId, tenantId, ruleSetCode, ruleSetVersion, overallStatus, ruleResults
            );
        }
    }
    
    /**
     * Checks if the rule set evaluation passed.
     */
    public boolean isPassed() {
        return overallStatus == RuleSetStatus.PASS;
    }
    
    /**
     * Checks if the rule set evaluation failed.
     */
    public boolean isFailed() {
        return overallStatus == RuleSetStatus.FAIL || overallStatus == RuleSetStatus.ERROR;
    }
    
    /**
     * Gets the count of failed rules.
     */
    public long getFailedRuleCount() {
        return ruleResults.stream()
                .filter(RuleResult::isFailure)
                .count();
    }
    
    /**
     * Gets the count of passed rules.
     */
    public long getPassedRuleCount() {
        return ruleResults.stream()
                .filter(RuleResult::isSuccess)
                .count();
    }
}

