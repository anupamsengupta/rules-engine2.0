package com.validationengine.persistence.provider;

import com.validationengine.core.domain.Rule;
import com.validationengine.core.domain.RuleSet;
import com.validationengine.persistence.entity.RuleEntity;
import com.validationengine.persistence.entity.RuleSetEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between JPA entities and domain models.
 */
@Component
public class RuleSetMapper {
    
    public RuleSet toDomain(RuleSetEntity entity) {
        List<Rule> rules = entity.getRules().stream()
                .filter(rule -> rule.getActive() != null && rule.getActive())
                .map(this::toDomain)
                .collect(Collectors.toList());
        
        return new RuleSet(
                entity.getTenantId(),
                entity.getRuleSetCode(),
                entity.getVersion(),
                entity.getName(),
                entity.getDescription(),
                rules,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
    
    public Rule toDomain(RuleEntity entity) {
        return new Rule(
                entity.getTenantId(),
                entity.getRuleSet().getRuleSetCode(),
                entity.getRuleCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getExpression(),
                entity.getScriptLocation(),
                entity.getPriority(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
    
    public RuleSetEntity toEntity(RuleSet domain) {
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(domain.tenantId());
        entity.setRuleSetCode(domain.ruleSetCode());
        entity.setVersion(domain.version());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setCreatedBy(domain.createdBy());
        entity.setUpdatedBy(domain.updatedBy());
        
        List<RuleEntity> ruleEntities = domain.rules().stream()
                .map(rule -> toEntity(rule, entity))
                .collect(Collectors.toList());
        entity.setRules(ruleEntities);
        
        return entity;
    }
    
    public RuleEntity toEntity(Rule domain, RuleSetEntity ruleSetEntity) {
        RuleEntity entity = new RuleEntity();
        entity.setTenantId(domain.tenantId());
        entity.setRuleSet(ruleSetEntity);
        entity.setRuleCode(domain.ruleCode());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setType(domain.type());
        entity.setExpression(domain.expression());
        entity.setScriptLocation(domain.scriptLocation());
        entity.setPriority(domain.priority());
        entity.setActive(domain.active());
        entity.setCreatedBy(domain.createdBy());
        entity.setUpdatedBy(domain.updatedBy());
        return entity;
    }
    
    public RuleEntity toEntity(Rule domain) {
        RuleEntity entity = new RuleEntity();
        entity.setTenantId(domain.tenantId());
        entity.setRuleCode(domain.ruleCode());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setType(domain.type());
        entity.setExpression(domain.expression());
        entity.setScriptLocation(domain.scriptLocation());
        entity.setPriority(domain.priority());
        entity.setActive(domain.active());
        entity.setCreatedBy(domain.createdBy());
        entity.setUpdatedBy(domain.updatedBy());
        return entity;
    }
}

