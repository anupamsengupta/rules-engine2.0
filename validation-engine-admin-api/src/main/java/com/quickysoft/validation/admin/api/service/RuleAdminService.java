package com.quickysoft.validation.admin.api.service;

import com.quickysoft.validation.admin.api.dto.RuleRequest;
import com.quickysoft.validation.admin.api.exception.ResourceNotFoundException;
import com.quickysoft.validation.admin.api.mapper.RuleSetDtoMapper;
import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleSet;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.entity.RuleEntity;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.repository.RuleRepository;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing rules within rule sets with write-through caching.
 */
@Service
public class RuleAdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleAdminService.class);
    
    private final RuleSetRepository ruleSetRepository;
    private final RuleRepository ruleRepository;
    private final RuleSetMapper persistenceMapper;
    private final RuleSetDtoMapper dtoMapper;
    private final RuleSetCache cache;
    
    public RuleAdminService(
            RuleSetRepository ruleSetRepository,
            RuleRepository ruleRepository,
            RuleSetMapper persistenceMapper,
            RuleSetDtoMapper dtoMapper,
            RuleSetCache cache
    ) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleRepository = ruleRepository;
        this.persistenceMapper = persistenceMapper;
        this.dtoMapper = dtoMapper;
        this.cache = cache;
    }
    
    /**
     * Adds a rule to a rule set.
     */
    @Transactional
    public Rule addRule(String tenantId, String ruleSetCode, String version, RuleRequest request) {
        RuleSetEntity ruleSetEntity = ruleSetRepository.findByTenantIdAndCodeAndVersion(
                tenantId, ruleSetCode, version
        ).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                        tenantId, ruleSetCode, version)
        ));
        
        // Check if rule already exists
        List<RuleEntity> existing = ruleRepository.findByTenantIdAndRuleSetCodeAndRuleCode(
                tenantId, ruleSetCode, request.ruleCode()
        );
        if (!existing.isEmpty()) {
            throw new com.quickysoft.validation.admin.api.exception.ResourceConflictException(
                    String.format("Rule already exists: tenantId=%s, ruleSetCode=%s, ruleCode=%s",
                            tenantId, ruleSetCode, request.ruleCode())
            );
        }
        
        // Convert to domain model
        Rule domain = dtoMapper.toDomain(tenantId, ruleSetCode, request);
        
        // Convert to entity and add to rule set
        RuleEntity entity = persistenceMapper.toEntity(domain, ruleSetEntity);
        ruleSetEntity.getRules().add(entity);
        ruleSetEntity = ruleSetRepository.save(ruleSetEntity);
        
        // Convert back to domain model
        RuleSet ruleSet = persistenceMapper.toDomain(ruleSetEntity);
        Rule saved = ruleSet.rules().stream()
                .filter(r -> r.ruleCode().equals(request.ruleCode()))
                .findFirst()
                .orElseThrow();
        
        // Write-through cache
        cache.evictRuleSet(tenantId, ruleSetCode, version);
        cache.putRuleSet(ruleSet);
        logger.info("Added rule: tenantId={}, ruleSetCode={}, ruleCode={}", tenantId, ruleSetCode, request.ruleCode());
        
        return saved;
    }
    
    /**
     * Updates a rule in a rule set.
     */
    @Transactional
    public Rule updateRule(String tenantId, String ruleSetCode, String version, String ruleCode, RuleRequest request) {
        RuleSetEntity ruleSetEntity = ruleSetRepository.findByTenantIdAndCodeAndVersion(
                tenantId, ruleSetCode, version
        ).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                        tenantId, ruleSetCode, version)
        ));
        
        // Find existing rule
        RuleEntity existing = ruleSetEntity.getRules().stream()
                .filter(r -> r.getRuleCode().equals(ruleCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Rule not found: tenantId=%s, ruleSetCode=%s, ruleCode=%s",
                                tenantId, ruleSetCode, ruleCode)
                ));
        
        // Update rule
        existing.setName(request.name());
        existing.setDescription(request.description());
        existing.setPriority(request.priority());
        existing.setEnabled(request.enabled());
        existing.setSeverity(request.severity());
        existing.setFailureMessageTemplate(request.failureMessageTemplate());
        existing.setMetadata(request.metadata());
        existing.setApplicableContexts(
                request.applicableContexts() != null 
                        ? String.join(",", request.applicableContexts()) 
                        : null
        );
        
        if (request.ruleType() == com.quickysoft.validation.admin.api.dto.RuleRequest.RuleType.EXPRESSION) {
            existing.setExpression(request.expression());
        } else if (request.ruleType() == com.quickysoft.validation.admin.api.dto.RuleRequest.RuleType.GROOVY) {
            existing.setScriptLocationType(request.scriptLocationType());
            existing.setScriptReference(request.scriptReference());
        }
        
        ruleSetEntity = ruleSetRepository.save(ruleSetEntity);
        
        // Convert back to domain model
        RuleSet ruleSet = persistenceMapper.toDomain(ruleSetEntity);
        Rule updated = ruleSet.rules().stream()
                .filter(r -> r.ruleCode().equals(ruleCode))
                .findFirst()
                .orElseThrow();
        
        // Write-through cache
        cache.evictRuleSet(tenantId, ruleSetCode, version);
        cache.putRuleSet(ruleSet);
        logger.info("Updated rule: tenantId={}, ruleSetCode={}, ruleCode={}", tenantId, ruleSetCode, ruleCode);
        
        return updated;
    }
    
    /**
     * Deletes or deactivates a rule.
     */
    @Transactional
    public void deleteRule(String tenantId, String ruleSetCode, String version, String ruleCode) {
        RuleSetEntity ruleSetEntity = ruleSetRepository.findByTenantIdAndCodeAndVersion(
                tenantId, ruleSetCode, version
        ).orElseThrow(() -> new ResourceNotFoundException(
                String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                        tenantId, ruleSetCode, version)
        ));
        
        // Find and deactivate rule
        RuleEntity rule = ruleSetEntity.getRules().stream()
                .filter(r -> r.getRuleCode().equals(ruleCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Rule not found: tenantId=%s, ruleSetCode=%s, ruleCode=%s",
                                tenantId, ruleSetCode, ruleCode)
                ));
        
        // Soft delete: set enabled to false
        rule.setEnabled(false);
        ruleSetEntity = ruleSetRepository.save(ruleSetEntity);
        
        // Write-through cache
        RuleSet ruleSet = persistenceMapper.toDomain(ruleSetEntity);
        cache.evictRuleSet(tenantId, ruleSetCode, version);
        cache.putRuleSet(ruleSet);
        logger.info("Deactivated rule: tenantId={}, ruleSetCode={}, ruleCode={}", tenantId, ruleSetCode, ruleCode);
    }
}

