package com.quickysoft.validation.admin.api.service;

import com.quickysoft.validation.admin.api.dto.RuleSetRequest;
import com.quickysoft.validation.admin.api.exception.ResourceConflictException;
import com.quickysoft.validation.admin.api.exception.ResourceNotFoundException;
import com.quickysoft.validation.admin.api.mapper.RuleSetDtoMapper;
import com.quickysoft.validation.core.model.RuleSet;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing rule sets with write-through caching.
 */
@Service
public class RuleSetAdminService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleSetAdminService.class);
    
    private final RuleSetRepository repository;
    private final RuleSetMapper persistenceMapper;
    private final RuleSetDtoMapper dtoMapper;
    private final RuleSetCache cache;
    
    public RuleSetAdminService(
            RuleSetRepository repository,
            RuleSetMapper persistenceMapper,
            RuleSetDtoMapper dtoMapper,
            RuleSetCache cache
    ) {
        this.repository = repository;
        this.persistenceMapper = persistenceMapper;
        this.dtoMapper = dtoMapper;
        this.cache = cache;
    }
    
    /**
     * Creates a new rule set.
     */
    @Transactional
    public RuleSet create(String tenantId, RuleSetRequest request) {
        // Check if already exists
        if (repository.existsByTenantIdAndCodeAndVersion(tenantId, request.code(), request.version())) {
            throw new ResourceConflictException(
                    String.format("Rule set already exists: tenantId=%s, code=%s, version=%s",
                            tenantId, request.code(), request.version())
            );
        }
        
        // Convert to domain model
        RuleSet domain = dtoMapper.toDomain(tenantId, request);
        
        // Convert to entity and save
        RuleSetEntity entity = persistenceMapper.toEntity(domain);
        entity = repository.save(entity);
        
        // Convert back to domain model
        RuleSet saved = persistenceMapper.toDomain(entity);
        
        // Write-through cache
        cache.putRuleSet(saved);
        logger.info("Created rule set: tenantId={}, code={}, version={}", tenantId, request.code(), request.version());
        
        return saved;
    }
    
    /**
     * Gets a rule set by code and version.
     */
    public RuleSet getByCodeAndVersion(String tenantId, String code, String version) {
        RuleSetEntity entity = repository.findByTenantIdAndCodeAndVersion(tenantId, code, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                                tenantId, code, version)
                ));
        
        return persistenceMapper.toDomain(entity);
    }
    
    /**
     * Gets all rule sets for a tenant with pagination.
     */
    public Page<RuleSet> getAll(String tenantId, Pageable pageable) {
        return repository.findByTenantId(tenantId, pageable)
                .map(persistenceMapper::toDomain);
    }
    
    /**
     * Gets all versions of a rule set for a tenant.
     */
    public List<RuleSet> getByCode(String tenantId, String code) {
        return repository.findByTenantIdAndCode(tenantId, code).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }
    
    /**
     * Updates a rule set (upsert).
     */
    @Transactional
    public RuleSet update(String tenantId, String code, String version, RuleSetRequest request) {
        final RuleSetEntity entity = repository.findByTenantIdAndCodeAndVersion(tenantId, code, version)
                .orElse(null);
        
        if (entity == null) {
            // Create new if doesn't exist
            return create(tenantId, request);
        }
        
        // Update existing
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity.setUpdatedBy(request.updatedBy());
        
        // Update rules (simplified - in production, handle more carefully)
        entity.getRules().clear();
        RuleSet domain = dtoMapper.toDomain(tenantId, request);
        domain.rules().forEach(rule -> {
            entity.getRules().add(persistenceMapper.toEntity(rule, entity));
        });

        RuleSetEntity entity1 = repository.save(entity);
        RuleSet updated = persistenceMapper.toDomain(entity1);
        
        // Write-through cache
        cache.evictRuleSet(tenantId, code, version);
        cache.putRuleSet(updated);
        logger.info("Updated rule set: tenantId={}, code={}, version={}", tenantId, code, version);
        
        return updated;
    }
    
    /**
     * Deletes or deactivates a rule set.
     */
    @Transactional
    public void delete(String tenantId, String code, String version) {
        RuleSetEntity entity = repository.findByTenantIdAndCodeAndVersion(tenantId, code, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                                tenantId, code, version)
                ));
        
        // Soft delete: set active to false
        entity.setActive(false);
        repository.save(entity);
        
        // Evict from cache
        cache.evictRuleSet(tenantId, code, version);
        logger.info("Deactivated rule set: tenantId={}, code={}, version={}", tenantId, code, version);
    }
    
    /**
     * Hard deletes a rule set.
     */
    @Transactional
    public void hardDelete(String tenantId, String code, String version) {
        RuleSetEntity entity = repository.findByTenantIdAndCodeAndVersion(tenantId, code, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                                tenantId, code, version)
                ));
        
        repository.delete(entity);
        
        // Evict from cache
        cache.evictRuleSet(tenantId, code, version);
        logger.info("Hard deleted rule set: tenantId={}, code={}, version={}", tenantId, code, version);
    }
}

