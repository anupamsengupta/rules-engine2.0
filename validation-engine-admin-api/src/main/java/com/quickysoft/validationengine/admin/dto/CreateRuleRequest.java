package com.quickysoft.validationengine.admin.dto;

import com.quickysoft.validationengine.core.domain.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a rule.
 */
public record CreateRuleRequest(
        @NotBlank(message = "ruleCode is required")
        String ruleCode,
        @NotBlank(message = "name is required")
        String name,
        String description,
        @NotNull(message = "type is required")
        RuleType type,
        String expression,
        String scriptLocation,
        Integer priority,
        Boolean active
) {
}

