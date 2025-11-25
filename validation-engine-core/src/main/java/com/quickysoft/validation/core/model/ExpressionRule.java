package com.quickysoft.validation.core.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Expression-based rule that evaluates a boolean expression (e.g., SpEL).
 */
public record ExpressionRule(
        UUID id,
        String tenantId,
        String ruleCode,
        String name,
        String description,
        Integer priority,
        boolean enabled,
        Severity severity,
        Set<String> applicableContexts,
        Map<String, String> metadata,
        String failureMessageTemplate,
        String expression,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
        ) implements Rule {
    
    public ExpressionRule {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (ruleCode == null || ruleCode.isBlank()) {
            throw new IllegalArgumentException("ruleCode cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression cannot be null or blank");
        }
        if (priority == null) {
            priority = 0;
        }
        if (severity == null) {
            severity = Severity.ERROR;
        }
        if (applicableContexts == null) {
            applicableContexts = Set.of();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Creates a builder for ExpressionRule.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for ExpressionRule.
     */
    public static class Builder {
        private UUID id;
        private String tenantId;
        private String ruleCode;
        private String name;
        private String description;
        private Integer priority = 0;
        private boolean enabled = true;
        private Severity severity = Severity.ERROR;
        private Set<String> applicableContexts = Set.of();
        private Map<String, String> metadata = Map.of();
        private String failureMessageTemplate;
        private String expression;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder ruleCode(String ruleCode) {
            this.ruleCode = ruleCode;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder applicableContexts(Set<String> applicableContexts) {
            this.applicableContexts = applicableContexts != null ? applicableContexts : Set.of();
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata != null ? metadata : Map.of();
            return this;
        }
        
        public Builder failureMessageTemplate(String failureMessageTemplate) {
            this.failureMessageTemplate = failureMessageTemplate;
            return this;
        }
        
        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }
        
        public ExpressionRule build() {
            if (id == null) {
                id = UUID.randomUUID();
            }
            return new ExpressionRule(
                    id, tenantId, ruleCode, name, description, priority, enabled,
                    severity, applicableContexts, metadata, failureMessageTemplate, expression, null, null
            );
        }
    }
}

