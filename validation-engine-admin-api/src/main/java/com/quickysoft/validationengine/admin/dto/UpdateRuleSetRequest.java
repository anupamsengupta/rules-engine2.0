package com.quickysoft.validationengine.admin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request DTO for updating a rule set.
 */
public record UpdateRuleSetRequest(
        @NotBlank(message = "name is required")
        String name,
        String description,
        @Valid
        List<CreateRuleRequest> rules,
        String updatedBy
) {
}

