package com.validationengine.core.evaluation;

import java.util.Map;

/**
 * Result of evaluating a single rule.
 */
public record RuleEvaluationResult(
        String ruleCode,
        boolean success,
        String code,
        String message,
        Map<String, Object> details
) {
    public RuleEvaluationResult {
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode cannot be null or blank");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }
        if (details == null) {
            details = Map.of();
        }
    }
    
    public static RuleEvaluationResult success(String ruleCode, String code, String message) {
        return new RuleEvaluationResult(ruleCode, true, code, message, Map.of());
    }
    
    public static RuleEvaluationResult failure(String ruleCode, String code, String message) {
        return new RuleEvaluationResult(ruleCode, false, code, message, Map.of());
    }
    
    public static RuleEvaluationResult failure(String ruleCode, String code, String message, Map<String, Object> details) {
        return new RuleEvaluationResult(ruleCode, false, code, message, details);
    }
}

