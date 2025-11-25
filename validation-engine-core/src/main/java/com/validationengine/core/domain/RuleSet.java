package com.validationengine.core.domain;

import java.time.Instant;
import java.util.List;

/**
 * A collection of rules grouped together for a specific validation purpose.
 * Multi-tenant: belongs to a tenantId.
 */
public record RuleSet(
        String tenantId,
        String ruleSetCode,
        String version,
        String name,
        String description,
        List<Rule> rules,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    public RuleSet {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (ruleSetCode == null || ruleSetCode.isBlank()) {
            throw new IllegalArgumentException("ruleSetCode cannot be null or blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version cannot be null or blank");
        }
        if (rules == null) {
            rules = List.of();
        }
    }
}

