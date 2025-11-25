package com.validationengine.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for creating a rule set.
 */
public record CreateRuleSetRequest(
        @NotBlank(message = "ruleSetCode is required")
        String ruleSetCode,
        @NotBlank(message = "version is required")
        String version,
        @NotBlank(message = "name is required")
        String name,
        String description,
        @NotNull(message = "rules cannot be null")
        @Valid
        List<CreateRuleRequest> rules,
        String createdBy
) {
}

