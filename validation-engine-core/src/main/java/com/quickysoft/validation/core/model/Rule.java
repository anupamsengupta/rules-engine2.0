package com.quickysoft.validation.core.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Sealed interface representing a validation rule.
 * 
 * Rules can be either expression-based or Groovy script-based.
 * All rules are multi-tenant and belong to a specific tenant.
 */
public sealed interface Rule permits ExpressionRule, GroovyScriptRule {
    
    /**
     * Unique identifier for the rule.
     */
    UUID id();
    
    /**
     * Tenant identifier - all rules are scoped to a tenant.
     */
    String tenantId();
    
    /**
     * Unique code for the rule within a rule set and tenant.
     */
    String ruleCode();
    
    /**
     * Human-readable name of the rule.
     */
    String name();
    
    /**
     * Optional description of what the rule validates.
     */
    String description();
    
    /**
     * Priority for rule execution order (lower numbers execute first).
     */
    Integer priority();
    
    /**
     * Whether the rule is enabled.
     */
    boolean enabled();
    
    /**
     * Severity level of the rule (INFO, WARN, ERROR).
     */
    Severity severity();
    
    /**
     * Optional set of context tags that determine when this rule is applicable.
     * If empty, rule applies to all contexts.
     */
    Set<String> applicableContexts();
    
    /**
     * Optional metadata map for additional rule information.
     */
    Map<String, String> metadata();
    
    /**
     * Optional template for failure message.
     * May contain placeholders that are replaced during evaluation.
     */
    String failureMessageTemplate();

    LocalDateTime createdAt();
    LocalDateTime updatedAt();
}

