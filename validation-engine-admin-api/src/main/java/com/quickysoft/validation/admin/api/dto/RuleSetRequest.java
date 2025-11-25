package com.quickysoft.validation.admin.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for creating or updating a rule set.
 */
public record RuleSetRequest(
        @NotBlank(message = "code is required")
        String code,
        
        @NotBlank(message = "name is required")
        String name,
        
        String description,
        
        @NotBlank(message = "version is required")
        String version,
        
        Boolean active,
        
        @NotNull(message = "rules cannot be null")
        @Valid
        List<RuleRequest> rules,
        
        String createdBy,
        String updatedBy
) {
    public RuleSetRequest {
        if (active == null) {
            active = true;
        }
        if (rules == null) {
            rules = List.of();
        }
    }
}

