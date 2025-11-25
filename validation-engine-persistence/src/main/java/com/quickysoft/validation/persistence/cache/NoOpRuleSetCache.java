package com.quickysoft.validation.persistence.cache;

import com.quickysoft.validation.core.model.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * No-op implementation of RuleSetCache.
 * 
 * Used when Redis is not available or caching is disabled.
 * All operations are no-ops, so the provider will always fall back to JPA.
 */
public class NoOpRuleSetCache implements RuleSetCache {
    
    private static final Logger logger = LoggerFactory.getLogger(NoOpRuleSetCache.class);
    
    @Override
    public RuleSet getRuleSet(String tenantId, String ruleSetCode, String version) {
        logger.debug("NoOp cache: getRuleSet({}, {}, {}) - returning null", tenantId, ruleSetCode, version);
        return null;
    }
    
    @Override
    public void putRuleSet(RuleSet ruleSet) {
        logger.debug("NoOp cache: putRuleSet({}, {}, {}) - no-op", 
                ruleSet.tenantId(), ruleSet.code(), ruleSet.version());
        // No-op
    }
    
    @Override
    public void evictRuleSet(String tenantId, String ruleSetCode, String version) {
        logger.debug("NoOp cache: evictRuleSet({}, {}, {}) - no-op", tenantId, ruleSetCode, version);
        // No-op
    }
}

