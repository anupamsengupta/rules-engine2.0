package com.quickysoft.validation.admin.api.dto;

import com.quickysoft.validation.core.model.ScriptLocationType;
import com.quickysoft.validation.core.model.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

/**
 * Request DTO for creating or updating a rule.
 */
public record RuleRequest(
        @NotBlank(message = "ruleCode is required")
        String ruleCode,
        
        @NotBlank(message = "name is required")
        String name,
        
        String description,
        
        Integer priority,
        
        Boolean enabled,
        
        @NotNull(message = "severity is required")
        Severity severity,
        
        @NotNull(message = "ruleType is required")
        RuleType ruleType,
        
        // Expression rule fields
        String expression,
        
        // Groovy script rule fields
        ScriptLocationType scriptLocationType,
        String scriptReference,
        
        // Common fields
        String failureMessageTemplate,
        Set<String> applicableContexts,
        Map<String, String> metadata
) {
    public RuleRequest {
        if (enabled == null) {
            enabled = true;
        }
        if (priority == null) {
            priority = 0;
        }
        if (applicableContexts == null) {
            applicableContexts = Set.of();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
    
    /**
     * Rule type enum for DTO.
     */
    public enum RuleType {
        EXPRESSION,
        GROOVY
    }
}

