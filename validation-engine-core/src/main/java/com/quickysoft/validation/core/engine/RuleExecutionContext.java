package com.quickysoft.validation.core.engine;

import java.util.Map;
import java.util.UUID;

/**
 * Execution context provided to Groovy validation scripts.
 * 
 * Contains metadata about the rule execution that scripts can use for
 * tenant-aware logic, logging, or custom validation behavior.
 * 
 * <p>This context is automatically injected into Groovy scripts as the
 * {@code executionContext} variable, providing access to:
 * <ul>
 *   <li>{@code tenantId} - The tenant identifier for multi-tenant logic</li>
 *   <li>{@code ruleSetCode} - The rule set code</li>
 *   <li>{@code ruleCode} - The rule code</li>
 *   <li>{@code ruleName} - The rule name</li>
 *   <li>{@code metadata} - Rule metadata map</li>
 * </ul>
 * 
 * <p>Example usage in Groovy script:
 * <pre>{@code
 * if (executionContext.tenantId() == 'tenant-premium') {
 *     // Premium tenant logic
 * }
 * def customValue = executionContext.getMetadata('customKey')
 * }</pre>
 */
public record RuleExecutionContext(
        UUID ruleId,
        String tenantId,
        String ruleSetCode,
        String ruleCode,
        String ruleName,
        Map<String, String> metadata
) {
    public RuleExecutionContext {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        // ruleSetCode can be null/blank if not available
        if (ruleSetCode == null) {
            ruleSetCode = "unknown";
        }
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode cannot be null or blank");
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
    
    /**
     * Gets a metadata value by key.
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Checks if a metadata key exists.
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
}

