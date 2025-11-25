package com.quickysoft.validationengine.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

/**
 * DTO for RuleSet operations.
 */
public record RuleSetDto(
        String tenantId,
        @NotBlank(message = "ruleSetCode is required")
        String ruleSetCode,
        @NotBlank(message = "version is required")
        String version,
        @NotBlank(message = "name is required")
        String name,
        String description,
        @NotNull(message = "rules cannot be null")
        List<RuleDto> rules,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}

