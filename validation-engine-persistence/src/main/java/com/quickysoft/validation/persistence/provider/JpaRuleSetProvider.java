package com.quickysoft.validation.persistence.provider;

import com.quickysoft.validation.core.model.RuleSet;
import com.quickysoft.validation.core.provider.RuleSetProvider;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * JPA-based implementation of RuleSetProvider with Redis-first caching strategy.
 * 
 * Uses write-through caching: checks Redis first, falls back to JPA if not found,
 * then caches the result in Redis.
 */
@Component
public class JpaRuleSetProvider implements RuleSetProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JpaRuleSetProvider.class);
    
    private final RuleSetCache cache;
    private final RuleSetRepository repository;
    private final RuleSetMapper mapper;
    
    public JpaRuleSetProvider(
            RuleSetCache cache,
            RuleSetRepository repository,
            RuleSetMapper mapper
    ) {
        this.cache = cache;
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public RuleSet getRuleSet(String tenantId, String ruleSetCode, String version) {
        // Try cache first
        RuleSet cached = cache.getRuleSet(tenantId, ruleSetCode, version);
        if (cached != null) {
            logger.debug("Rule set found in cache: tenantId={}, code={}, version={}", 
                    tenantId, ruleSetCode, version);
            return cached;
        }
        
        // Fallback to JPA
        logger.debug("Rule set not in cache, querying JPA: tenantId={}, code={}, version={}", 
                tenantId, ruleSetCode, version);
        
        RuleSetEntity entity = repository.findByTenantIdAndCodeAndVersion(tenantId, ruleSetCode, version)
                .orElse(null);
        
        if (entity == null) {
            logger.debug("Rule set not found in JPA: tenantId={}, code={}, version={}", 
                    tenantId, ruleSetCode, version);
            return null;
        }
        
        // Convert to domain model
        RuleSet ruleSet = mapper.toDomain(entity);
        
        // Cache for future use
        cache.putRuleSet(ruleSet);
        
        return ruleSet;
    }
    
    @Override
    public void evictRuleSet(String tenantId, String ruleSetCode, String version) {
        logger.debug("Evicting rule set from cache: tenantId={}, code={}, version={}", 
                tenantId, ruleSetCode, version);
        cache.evictRuleSet(tenantId, ruleSetCode, version);
    }
    
    @Override
    public void cacheRuleSet(RuleSet ruleSet) {
        logger.debug("Caching rule set: tenantId={}, code={}, version={}", 
                ruleSet.tenantId(), ruleSet.code(), ruleSet.version());
        cache.putRuleSet(ruleSet);
    }
}

