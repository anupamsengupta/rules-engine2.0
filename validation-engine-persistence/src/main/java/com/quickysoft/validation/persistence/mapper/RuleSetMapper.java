package com.quickysoft.validation.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickysoft.validation.core.model.*;
import com.quickysoft.validation.persistence.entity.RuleEntity;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.entity.RuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper between JPA entities and canonical domain models.
 */
@Component
public class RuleSetMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleSetMapper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Converts RuleSetEntity to RuleSet domain model.
     */
    public RuleSet toDomain(RuleSetEntity entity) {
        if (entity == null) {
            return null;
        }
        
        List<Rule> rules = entity.getRules().stream()
                .filter(rule -> rule.getEnabled() != null && rule.getEnabled())
                .map(this::toDomain)
                .collect(Collectors.toList());
        
        return RuleSet.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .version(entity.getVersion())
                .active(entity.getActive() != null ? entity.getActive() : true)
                .rules(rules)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
    
    /**
     * Converts RuleEntity to Rule domain model.
     */
    public Rule toDomain(RuleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        Set<String> applicableContexts = parseApplicableContexts(entity.getApplicableContexts());
        Map<String, String> metadata = entity.getMetadata() != null ? entity.getMetadata() : Map.of();
        
        if (entity.getRuleType() == RuleType.EXPRESSION) {
            return ExpressionRule.builder()
                    .id(entity.getId())
                    .tenantId(entity.getTenantId())
                    .ruleCode(entity.getRuleCode())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .priority(entity.getPriority())
                    .enabled(entity.getEnabled() != null ? entity.getEnabled() : true)
                    .severity(entity.getSeverity() != null ? entity.getSeverity() : Severity.ERROR)
                    .applicableContexts(applicableContexts)
                    .metadata(metadata)
                    .failureMessageTemplate(entity.getFailureMessageTemplate())
                    .expression(entity.getExpression())
                    .build();
        } else if (entity.getRuleType() == RuleType.GROOVY) {
            return GroovyScriptRule.builder()
                    .id(entity.getId())
                    .tenantId(entity.getTenantId())
                    .ruleCode(entity.getRuleCode())
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .priority(entity.getPriority())
                    .enabled(entity.getEnabled() != null ? entity.getEnabled() : true)
                    .severity(entity.getSeverity() != null ? entity.getSeverity() : Severity.ERROR)
                    .applicableContexts(applicableContexts)
                    .metadata(metadata)
                    .failureMessageTemplate(entity.getFailureMessageTemplate())
                    .scriptLocationType(entity.getScriptLocationType())
                    .scriptReference(entity.getScriptReference())
                    .build();
        } else {
            throw new IllegalArgumentException("Unknown rule type: " + entity.getRuleType());
        }
    }
    
    /**
     * Converts RuleSet domain model to RuleSetEntity.
     */
    public RuleSetEntity toEntity(RuleSet domain) {
        if (domain == null) {
            return null;
        }
        
        RuleSetEntity entity = new RuleSetEntity();
        entity.setId(domain.id());
        entity.setTenantId(domain.tenantId());
        entity.setCode(domain.code());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setVersion(domain.version());
        entity.setActive(domain.active());
        entity.setCreatedBy(domain.createdBy());
        entity.setUpdatedBy(domain.updatedBy());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        
        List<RuleEntity> ruleEntities = domain.rules().stream()
                .map(rule -> toEntity(rule, entity))
                .collect(Collectors.toList());
        entity.setRules(ruleEntities);
        
        return entity;
    }
    
    /**
     * Converts Rule domain model to RuleEntity.
     */
    public RuleEntity toEntity(Rule domain, RuleSetEntity ruleSetEntity) {
        if (domain == null) {
            return null;
        }
        
        RuleEntity entity = new RuleEntity();
        entity.setId(domain.id());
        entity.setTenantId(domain.tenantId());
        entity.setRuleSet(ruleSetEntity);
        entity.setRuleCode(domain.ruleCode());
        entity.setName(domain.name());
        entity.setDescription(domain.description());
        entity.setPriority(domain.priority());
        entity.setEnabled(domain.enabled());
        entity.setSeverity(domain.severity());
        entity.setFailureMessageTemplate(domain.failureMessageTemplate());
        entity.setMetadata(domain.metadata());
        entity.setApplicableContexts(formatApplicableContexts(domain.applicableContexts()));
        entity.setCreatedBy(domain.createdAt() != null ? "system" : null);
        entity.setUpdatedBy(domain.updatedAt() != null ? "system" : null);
        
        if (domain instanceof ExpressionRule expressionRule) {
            entity.setRuleType(RuleType.EXPRESSION);
            entity.setExpression(expressionRule.expression());
        } else if (domain instanceof GroovyScriptRule groovyRule) {
            entity.setRuleType(RuleType.GROOVY);
            entity.setScriptLocationType(groovyRule.scriptLocationType());
            entity.setScriptReference(groovyRule.scriptReference());
        } else {
            throw new IllegalArgumentException("Unknown rule type: " + domain.getClass().getSimpleName());
        }
        
        return entity;
    }
    
    /**
     * Parses applicable contexts from string (comma-separated or JSON array).
     */
    private Set<String> parseApplicableContexts(String contexts) {
        if (contexts == null || contexts.isBlank()) {
            return Set.of();
        }
        
        try {
            // Try JSON array first
            List<String> list = objectMapper.readValue(contexts, new TypeReference<List<String>>() {});
            return new HashSet<>(list);
        } catch (Exception e) {
            // Fall back to comma-separated
            return Arrays.stream(contexts.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }
    }
    
    /**
     * Formats applicable contexts to string (JSON array).
     */
    private String formatApplicableContexts(Set<String> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(new ArrayList<>(contexts));
        } catch (Exception e) {
            logger.error("Failed to format applicable contexts", e);
            return String.join(",", contexts);
        }
    }
}

