package com.quickysoft.validation.persistence.cache;

import com.quickysoft.validation.core.model.RuleSet;

/**
 * Cache interface for rule sets.
 * 
 * Provides tenant-aware caching operations for rule sets.
 */
public interface RuleSetCache {
    
    /**
     * Retrieves a rule set from cache.
     *
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the version
     * @return the rule set, or null if not found
     */
    RuleSet getRuleSet(String tenantId, String ruleSetCode, String version);
    
    /**
     * Caches a rule set.
     *
     * @param ruleSet the rule set to cache
     */
    void putRuleSet(RuleSet ruleSet);
    
    /**
     * Evicts a rule set from cache.
     *
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the version
     */
    void evictRuleSet(String tenantId, String ruleSetCode, String version);
}

