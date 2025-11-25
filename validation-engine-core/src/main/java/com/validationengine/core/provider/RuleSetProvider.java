package com.validationengine.core.provider;

import com.validationengine.core.domain.RuleSet;

/**
 * Provider interface for retrieving rule sets.
 * Implementations should use Redis-first strategy with fallback to persistence layer.
 */
public interface RuleSetProvider {
    
    /**
     * Retrieves a rule set by tenant, code, and version.
     * Should first check Redis cache, then fall back to persistence if not found.
     *
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the version
     * @return the rule set, or null if not found
     */
    RuleSet getRuleSet(String tenantId, String ruleSetCode, String version);
    
    /**
     * Evicts a rule set from cache.
     *
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the version
     */
    void evictRuleSet(String tenantId, String ruleSetCode, String version);
    
    /**
     * Caches a rule set.
     *
     * @param ruleSet the rule set to cache
     */
    void cacheRuleSet(RuleSet ruleSet);
}

