package com.quickysoft.validation.core.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Groovy script-based rule that executes an external script.
 */
public record GroovyScriptRule(
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
        ScriptLocationType scriptLocationType,
        String scriptReference,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Rule {
    
    public GroovyScriptRule {
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
            throw new IllegalArgumentException("name cannot be blank");
        }
        if (scriptLocationType == null) {
            throw new IllegalArgumentException("scriptLocationType cannot be null");
        }
        if (scriptReference == null || scriptReference.isBlank()) {
            throw new IllegalArgumentException("scriptReference cannot be null or blank");
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
        }if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }

    }
    
    /**
     * Creates a builder for GroovyScriptRule.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for GroovyScriptRule.
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
        private ScriptLocationType scriptLocationType;
        private String scriptReference;
        
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
        
        public Builder scriptLocationType(ScriptLocationType scriptLocationType) {
            this.scriptLocationType = scriptLocationType;
            return this;
        }
        
        public Builder scriptReference(String scriptReference) {
            this.scriptReference = scriptReference;
            return this;
        }
        
        public GroovyScriptRule build() {
            if (id == null) {
                id = UUID.randomUUID();
            }
            return new GroovyScriptRule(
                    id, tenantId, ruleCode, name, description, priority, enabled,
                    severity, applicableContexts, metadata, failureMessageTemplate,
                    scriptLocationType, scriptReference, null, null
            );
        }
    }
}

