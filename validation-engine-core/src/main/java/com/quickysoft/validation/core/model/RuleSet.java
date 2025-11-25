package com.quickysoft.validation.core.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A collection of rules grouped together for a specific validation purpose.
 * 
 * Rule sets are multi-tenant and versioned. Each rule set has a unique code
 * within a tenant, and multiple versions can exist.
 * 
 * Rules within a rule set are ordered by priority and executed in sequence.
 */
public record RuleSet(
        UUID id,
        String tenantId,
        String code,
        String name,
        String description,
        String version,
        boolean active,
        List<Rule> rules,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    
    public RuleSet {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId cannot be null or blank");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version cannot be null or blank");
        }
        if (rules == null) {
            rules = List.of();
        }
    }
    
    /**
     * Creates a builder for RuleSet.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for RuleSet.
     */
    public static class Builder {
        private UUID id;
        private String tenantId;
        private String code;
        private String name;
        private String description;
        private String version;
        private boolean active = true;
        private List<Rule> rules = List.of();
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;
        
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder code(String code) {
            this.code = code;
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
        
        public Builder version(String version) {
            this.version = version;
            return this;
        }
        
        public Builder active(boolean active) {
            this.active = active;
            return this;
        }
        
        public Builder rules(List<Rule> rules) {
            this.rules = rules != null ? rules : List.of();
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }
        
        public RuleSet build() {
            if (id == null) {
                id = UUID.randomUUID();
            }
            Instant now = Instant.now();
            if (createdAt == null) {
                createdAt = now;
            }
            if (updatedAt == null) {
                updatedAt = now;
            }
            return new RuleSet(
                    id, tenantId, code, name, description, version, active,
                    rules, createdAt, updatedAt, createdBy, updatedBy
            );
        }
    }
}

