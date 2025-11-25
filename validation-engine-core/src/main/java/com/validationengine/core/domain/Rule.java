package com.validationengine.core.domain;

import java.time.Instant;

/**
 * A single validation rule within a rule set.
 * Can be either expression-based or Groovy script-based.
 */
public record Rule(
        String tenantId,
        String ruleSetCode,
        String ruleCode,
        String name,
        String description,
        RuleType type,
        String expression,
        String scriptLocation,
        Integer priority,
        Boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    public Rule {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (ruleSetCode == null || ruleSetCode.isBlank()) {
            throw new IllegalArgumentException("ruleSetCode cannot be null or blank");
        }
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (active == null) {
            active = true;
        }
    }
    
    public boolean isExpressionBased() {
        return type == RuleType.EXPRESSION;
    }
    
    public boolean isGroovyScriptBased() {
        return type == RuleType.GROOVY_SCRIPT;
    }
}

