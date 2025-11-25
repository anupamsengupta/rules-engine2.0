package com.validationengine.core.evaluation;

import java.util.List;

/**
 * Result of evaluating a complete rule set.
 */
public record RuleSetEvaluationResult(
        String ruleSetCode,
        String version,
        RuleSetEvaluationStatus status,
        List<RuleEvaluationResult> ruleResults
) {
    public RuleSetEvaluationResult {
        if (ruleSetCode == null || ruleSetCode.isBlank()) {
            throw new IllegalArgumentException("ruleSetCode cannot be null or blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version cannot be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
        }
        if (ruleResults == null) {
            ruleResults = List.of();
        }
    }
}

