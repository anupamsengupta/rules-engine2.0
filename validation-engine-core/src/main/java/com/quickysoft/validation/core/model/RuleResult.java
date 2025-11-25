package com.quickysoft.validation.core.model;

import java.util.Map;
import java.util.UUID;

/**
 * Result of evaluating a single rule.
 * 
 * Contains the rule identification, evaluation status, message, and optional details.
 * The tenantId is included for multi-tenant logging and tracking.
 */
public record RuleResult(
        UUID ruleId,
        String tenantId,
        String ruleCode,
        String ruleName,
        Severity severity,
        RuleStatus status,
        String message,
        Map<String, Object> details,
        Throwable error
) {
    
    public RuleResult {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId cannot be null");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode cannot be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        if (details == null) {
            details = Map.of();
        }
    }
    
    /**
     * Creates a successful rule result.
     */
    public static RuleResult passed(UUID ruleId, String tenantId, String ruleCode, String ruleName, Severity severity) {
        return new RuleResult(
                ruleId, tenantId, ruleCode, ruleName, severity, RuleStatus.PASSED,
                "Rule passed", Map.of(), null
        );
    }
    
    /**
     * Creates a failed rule result.
     */
    public static RuleResult failed(UUID ruleId, String tenantId, String ruleCode, String ruleName, 
                                    Severity severity, String message) {
        return new RuleResult(
                ruleId, tenantId, ruleCode, ruleName, severity, RuleStatus.FAILED,
                message, Map.of(), null
        );
    }
    
    /**
     * Creates a failed rule result with details.
     */
    public static RuleResult failed(UUID ruleId, String tenantId, String ruleCode, String ruleName,
                                    Severity severity, String message, Map<String, Object> details) {
        return new RuleResult(
                ruleId, tenantId, ruleCode, ruleName, severity, RuleStatus.FAILED,
                message, details, null
        );
    }
    
    /**
     * Creates a skipped rule result.
     */
    public static RuleResult skipped(UUID ruleId, String tenantId, String ruleCode, String ruleName, String reason) {
        return new RuleResult(
                ruleId, tenantId, ruleCode, ruleName, Severity.INFO, RuleStatus.SKIPPED,
                reason != null ? reason : "Rule skipped", Map.of(), null
        );
    }
    
    /**
     * Creates an error rule result (exception during evaluation).
     */
    public static RuleResult error(UUID ruleId, String tenantId, String ruleCode, String ruleName, 
                                   Throwable error) {
        return new RuleResult(
                ruleId, tenantId, ruleCode, ruleName, Severity.ERROR, RuleStatus.ERROR,
                error != null ? error.getMessage() : "Error during rule evaluation",
                Map.of(), error
        );
    }
    
    /**
     * Checks if the rule result indicates failure.
     */
    public boolean isFailure() {
        return status == RuleStatus.FAILED || status == RuleStatus.ERROR;
    }
    
    /**
     * Checks if the rule result indicates success.
     */
    public boolean isSuccess() {
        return status == RuleStatus.PASSED;
    }
}

