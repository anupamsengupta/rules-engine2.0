package com.quickysoft.validationengine.persistence.provider;

import com.quickysoft.validationengine.core.domain.RuleSet;
import com.quickysoft.validationengine.core.provider.RuleSetProvider;
import com.quickysoft.validationengine.persistence.cache.RedisCacheService;
import com.quickysoft.validationengine.persistence.repository.RuleSetRepository;
import org.springframework.stereotype.Component;

/**
 * Implementation of RuleSetProvider that uses Redis-first strategy with JPA fallback.
 */
@Component
public class JpaRuleSetProvider implements RuleSetProvider {
    
    private final RedisCacheService cacheService;
    private final RuleSetRepository ruleSetRepository;
    private final RuleSetMapper ruleSetMapper;
    
    public JpaRuleSetProvider(
            RedisCacheService cacheService,
            RuleSetRepository ruleSetRepository,
            RuleSetMapper ruleSetMapper
    ) {
        this.cacheService = cacheService;
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetMapper = ruleSetMapper;
    }
    
    @Override
    public RuleSet getRuleSet(String tenantId, String ruleSetCode, String version) {
        // Try cache first
        return cacheService.get(tenantId, ruleSetCode, version)
                .orElseGet(() -> {
                    // Fallback to JPA
                    return ruleSetRepository
                            .findByTenantIdAndRuleSetCodeAndVersion(tenantId, ruleSetCode, version)
                            .map(ruleSetMapper::toDomain)
                            .map(ruleSet -> {
                                // Cache for future use
                                cacheService.put(ruleSet);
                                return ruleSet;
                            })
                            .orElse(null);
                });
    }
    
    @Override
    public void evictRuleSet(String tenantId, String ruleSetCode, String version) {
        cacheService.evict(tenantId, ruleSetCode, version);
    }
    
    @Override
    public void cacheRuleSet(RuleSet ruleSet) {
        cacheService.put(ruleSet);
    }
}

