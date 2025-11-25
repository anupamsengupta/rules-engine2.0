package com.validationengine.admin.mapper;

import com.validationengine.admin.dto.RuleDto;
import com.validationengine.admin.dto.RuleSetDto;
import com.validationengine.core.domain.Rule;
import com.validationengine.core.domain.RuleSet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between domain models and DTOs.
 */
@Component
public class RuleSetDtoMapper {
    
    public RuleSetDto toDto(RuleSet domain) {
        List<RuleDto> ruleDtos = domain.rules().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return new RuleSetDto(
                domain.tenantId(),
                domain.ruleSetCode(),
                domain.version(),
                domain.name(),
                domain.description(),
                ruleDtos,
                domain.createdAt(),
                domain.updatedAt(),
                domain.createdBy(),
                domain.updatedBy()
        );
    }
    
    public RuleDto toDto(Rule domain) {
        return new RuleDto(
                domain.tenantId(),
                domain.ruleSetCode(),
                domain.ruleCode(),
                domain.name(),
                domain.description(),
                domain.type(),
                domain.expression(),
                domain.scriptLocation(),
                domain.priority(),
                domain.active(),
                domain.createdAt(),
                domain.updatedAt(),
                domain.createdBy(),
                domain.updatedBy()
        );
    }
    
    public RuleSet toDomain(RuleSetDto dto) {
        List<Rule> rules = dto.rules().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
        
        return new RuleSet(
                dto.tenantId(),
                dto.ruleSetCode(),
                dto.version(),
                dto.name(),
                dto.description(),
                rules,
                dto.createdAt(),
                dto.updatedAt(),
                dto.createdBy(),
                dto.updatedBy()
        );
    }
    
    public Rule toDomain(RuleDto dto) {
        return new Rule(
                dto.tenantId(),
                dto.ruleSetCode(),
                dto.ruleCode(),
                dto.name(),
                dto.description(),
                dto.type(),
                dto.expression(),
                dto.scriptLocation(),
                dto.priority(),
                dto.active(),
                dto.createdAt(),
                dto.updatedAt(),
                dto.createdBy(),
                dto.updatedBy()
        );
    }
}

