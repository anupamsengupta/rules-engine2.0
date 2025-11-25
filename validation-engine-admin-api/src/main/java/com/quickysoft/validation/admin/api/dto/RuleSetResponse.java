package com.quickysoft.validation.admin.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a rule set.
 */
public record RuleSetResponse(
        UUID id,
        String tenantId,
        String code,
        String name,
        String description,
        String version,
        Boolean active,
        List<RuleResponse> rules,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}

