package com.quickysoft.validation.admin.api.mapper;

import com.quickysoft.validation.admin.api.dto.*;
import com.quickysoft.validation.core.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between DTOs and canonical domain models.
 */
@Component
public class RuleSetDtoMapper {
    
    /**
     * Converts RuleSetRequest to RuleSet domain model.
     */
    public RuleSet toDomain(String tenantId, RuleSetRequest request) {
        List<Rule> rules = request.rules().stream()
                .map(ruleReq -> toDomain(tenantId, request.code(), ruleReq))
                .collect(Collectors.toList());
        
        return RuleSet.builder()
                .tenantId(tenantId)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .version(request.version())
                .active(request.active())
                .rules(rules)
                .createdBy(request.createdBy())
                .updatedBy(request.updatedBy())
                .build();
    }
    
    /**
     * Converts RuleRequest to Rule domain model.
     */
    public Rule toDomain(String tenantId, String ruleSetCode, RuleRequest request) {
        if (request.ruleType() == RuleRequest.RuleType.EXPRESSION) {
            return ExpressionRule.builder()
                    .tenantId(tenantId)
                    .ruleCode(request.ruleCode())
                    .name(request.name())
                    .description(request.description())
                    .priority(request.priority())
                    .enabled(request.enabled())
                    .severity(request.severity())
                    .applicableContexts(request.applicableContexts())
                    .metadata(request.metadata())
                    .failureMessageTemplate(request.failureMessageTemplate())
                    .expression(request.expression())
                    .build();
        } else if (request.ruleType() == RuleRequest.RuleType.GROOVY) {
            return GroovyScriptRule.builder()
                    .tenantId(tenantId)
                    .ruleCode(request.ruleCode())
                    .name(request.name())
                    .description(request.description())
                    .priority(request.priority())
                    .enabled(request.enabled())
                    .severity(request.severity())
                    .applicableContexts(request.applicableContexts())
                    .metadata(request.metadata())
                    .failureMessageTemplate(request.failureMessageTemplate())
                    .scriptLocationType(request.scriptLocationType())
                    .scriptReference(request.scriptReference())
                    .build();
        } else {
            throw new IllegalArgumentException("Unknown rule type: " + request.ruleType());
        }
    }
    
    /**
     * Converts RuleSet domain model to RuleSetResponse.
     */
    public RuleSetResponse toResponse(RuleSet domain) {
        List<RuleResponse> ruleResponses = domain.rules().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return new RuleSetResponse(
                domain.id(),
                domain.tenantId(),
                domain.code(),
                domain.name(),
                domain.description(),
                domain.version(),
                domain.active(),
                ruleResponses,
                domain.createdAt(),
                domain.updatedAt(),
                domain.createdBy(),
                domain.updatedBy()
        );
    }
    
    /**
     * Converts Rule domain model to RuleResponse.
     */
    public RuleResponse toResponse(Rule domain) {
        RuleResponse.RuleType ruleType = domain instanceof ExpressionRule 
                ? RuleResponse.RuleType.EXPRESSION 
                : RuleResponse.RuleType.GROOVY;
        
        String expression = domain instanceof ExpressionRule 
                ? ((ExpressionRule) domain).expression() 
                : null;
        
        ScriptLocationType scriptLocationType = domain instanceof GroovyScriptRule 
                ? ((GroovyScriptRule) domain).scriptLocationType() 
                : null;
        
        String scriptReference = domain instanceof GroovyScriptRule 
                ? ((GroovyScriptRule) domain).scriptReference() 
                : null;
        
        return new RuleResponse(
                domain.id(),
                domain.tenantId(),
                domain.ruleCode(),
                domain.name(),
                domain.description(),
                domain.priority(),
                domain.enabled(),
                domain.severity(),
                ruleType,
                expression,
                scriptLocationType,
                scriptReference,
                domain.failureMessageTemplate(),
                domain.applicableContexts(),
                domain.metadata(),
                null, // createdAt - would need to be added to domain model
                null, // updatedAt - would need to be added to domain model
                null, // createdBy
                null  // updatedBy
        );
    }
}

