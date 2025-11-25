package com.quickysoft.validation.admin.api.dto;

import com.quickysoft.validation.core.model.ScriptLocationType;
import com.quickysoft.validation.core.model.Severity;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Response DTO for a rule.
 */
public record RuleResponse(
        UUID id,
        String tenantId,
        String ruleCode,
        String name,
        String description,
        Integer priority,
        Boolean enabled,
        Severity severity,
        RuleType ruleType,
        
        // Expression rule fields
        String expression,
        
        // Groovy script rule fields
        ScriptLocationType scriptLocationType,
        String scriptReference,
        
        // Common fields
        String failureMessageTemplate,
        Set<String> applicableContexts,
        Map<String, String> metadata,
        
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    /**
     * Rule type enum for DTO.
     */
    public enum RuleType {
        EXPRESSION,
        GROOVY
    }
}

