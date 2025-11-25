package com.validationengine.admin.service;

import com.validationengine.core.domain.RuleSet;
import com.validationengine.core.provider.RuleSetProvider;
import com.validationengine.persistence.entity.RuleSetEntity;
import com.validationengine.persistence.provider.RuleSetMapper;
import com.validationengine.persistence.repository.RuleSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for managing rule sets with write-through caching.
 */
@Service
public class RuleSetService {
    
    private final RuleSetRepository ruleSetRepository;
    private final RuleSetMapper ruleSetMapper;
    private final RuleSetProvider ruleSetProvider;
    
    public RuleSetService(
            RuleSetRepository ruleSetRepository,
            RuleSetMapper ruleSetMapper,
            RuleSetProvider ruleSetProvider
    ) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetMapper = ruleSetMapper;
        this.ruleSetProvider = ruleSetProvider;
    }
    
    @Transactional
    public RuleSet create(RuleSet ruleSet) {
        // Check if already exists
        if (ruleSetRepository.existsByTenantIdAndRuleSetCodeAndVersion(
                ruleSet.tenantId(),
                ruleSet.ruleSetCode(),
                ruleSet.version()
        )) {
            throw new IllegalArgumentException(
                    String.format("Rule set already exists: %s/%s/%s",
                            ruleSet.tenantId(), ruleSet.ruleSetCode(), ruleSet.version())
            );
        }
        
        RuleSetEntity entity = ruleSetMapper.toEntity(ruleSet);
        entity = ruleSetRepository.save(entity);
        RuleSet saved = ruleSetMapper.toDomain(entity);
        
        // Write-through cache
        ruleSetProvider.cacheRuleSet(saved);
        
        return saved;
    }
    
    @Transactional
    public RuleSet update(String tenantId, String ruleSetCode, String version, RuleSet ruleSet) {
        final RuleSetEntity entity = ruleSetRepository
                .findByTenantIdAndRuleSetCodeAndVersion(tenantId, ruleSetCode, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Rule set not found: %s/%s/%s",
                                tenantId, ruleSetCode, version)
                ));
        
        // Update fields
        entity.setName(ruleSet.name());
        entity.setDescription(ruleSet.description());
        entity.setUpdatedBy(ruleSet.updatedBy());
        
        // Update rules (simplified - in production, handle more carefully)
        entity.getRules().clear();
        ruleSet.rules().forEach(rule -> {
            entity.getRules().add(ruleSetMapper.toEntity(rule, entity));
        });

        RuleSetEntity entity1 = ruleSetRepository.save(entity);
        RuleSet updated = ruleSetMapper.toDomain(entity1);
        
        // Write-through cache
        ruleSetProvider.evictRuleSet(tenantId, ruleSetCode, version);
        ruleSetProvider.cacheRuleSet(updated);
        
        return updated;
    }
    
    @Transactional
    public void delete(String tenantId, String ruleSetCode, String version) {
        RuleSetEntity entity = ruleSetRepository
                .findByTenantIdAndRuleSetCodeAndVersion(tenantId, ruleSetCode, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Rule set not found: %s/%s/%s",
                                tenantId, ruleSetCode, version)
                ));
        
        ruleSetRepository.delete(entity);
        
        // Evict from cache
        ruleSetProvider.evictRuleSet(tenantId, ruleSetCode, version);
    }
    
    public Optional<RuleSet> get(String tenantId, String ruleSetCode, String version) {
        return Optional.ofNullable(ruleSetProvider.getRuleSet(tenantId, ruleSetCode, version));
    }
}

