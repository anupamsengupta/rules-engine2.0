package com.quickysoft.validationengine.admin.controller;

import com.quickysoft.validationengine.admin.dto.CreateRuleSetRequest;
import com.quickysoft.validationengine.admin.dto.RuleSetDto;
import com.quickysoft.validationengine.admin.dto.UpdateRuleSetRequest;
import com.quickysoft.validationengine.admin.mapper.RuleSetDtoMapper;
import com.quickysoft.validationengine.admin.service.RuleSetService;
import com.quickysoft.validationengine.core.domain.Rule;
import com.quickysoft.validationengine.core.domain.RuleSet;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing rule sets.
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/rule-sets")
public class RuleSetController {
    
    private final RuleSetService ruleSetService;
    private final RuleSetDtoMapper dtoMapper;
    
    public RuleSetController(RuleSetService ruleSetService, RuleSetDtoMapper dtoMapper) {
        this.ruleSetService = ruleSetService;
        this.dtoMapper = dtoMapper;
    }
    
    @PostMapping
    public ResponseEntity<RuleSetDto> create(
            @PathVariable String tenantId,
            @Valid @RequestBody CreateRuleSetRequest request
    ) {
        List<Rule> rules = request.rules().stream()
                .map(ruleReq -> new Rule(
                        tenantId,
                        request.ruleSetCode(),
                        ruleReq.ruleCode(),
                        ruleReq.name(),
                        ruleReq.description(),
                        ruleReq.type(),
                        ruleReq.expression(),
                        ruleReq.scriptLocation(),
                        ruleReq.priority(),
                        ruleReq.active() != null ? ruleReq.active() : true,
                        null,
                        null,
                        request.createdBy(),
                        null
                ))
                .collect(Collectors.toList());
        
        RuleSet ruleSet = new RuleSet(
                tenantId,
                request.ruleSetCode(),
                request.version(),
                request.name(),
                request.description(),
                rules,
                Instant.now(),
                Instant.now(),
                request.createdBy(),
                request.createdBy()
        );
        
        RuleSet created = ruleSetService.create(ruleSet);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoMapper.toDto(created));
    }
    
    @GetMapping("/{ruleSetCode}/versions/{version}")
    public ResponseEntity<RuleSetDto> get(
            @PathVariable String tenantId,
            @PathVariable String ruleSetCode,
            @PathVariable String version
    ) {
        return ruleSetService.get(tenantId, ruleSetCode, version)
                .map(dtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{ruleSetCode}/versions/{version}")
    public ResponseEntity<RuleSetDto> update(
            @PathVariable String tenantId,
            @PathVariable String ruleSetCode,
            @PathVariable String version,
            @Valid @RequestBody UpdateRuleSetRequest request
    ) {
        RuleSet existing = ruleSetService.get(tenantId, ruleSetCode, version)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Rule set not found: %s/%s/%s", tenantId, ruleSetCode, version)
                ));
        
        List<Rule> rules = request.rules() != null ? request.rules().stream()
                .map(ruleReq -> new Rule(
                        tenantId,
                        ruleSetCode,
                        ruleReq.ruleCode(),
                        ruleReq.name(),
                        ruleReq.description(),
                        ruleReq.type(),
                        ruleReq.expression(),
                        ruleReq.scriptLocation(),
                        ruleReq.priority(),
                        ruleReq.active() != null ? ruleReq.active() : true,
                        existing.rules().stream()
                                .filter(r -> r.ruleCode().equals(ruleReq.ruleCode()))
                                .findFirst()
                                .map(Rule::createdAt)
                                .orElse(Instant.now()),
                        Instant.now(),
                        existing.rules().stream()
                                .filter(r -> r.ruleCode().equals(ruleReq.ruleCode()))
                                .findFirst()
                                .map(Rule::createdBy)
                                .orElse(null),
                        request.updatedBy()
                ))
                .collect(Collectors.toList()) : existing.rules();
        
        RuleSet updated = new RuleSet(
                tenantId,
                ruleSetCode,
                version,
                request.name(),
                request.description(),
                rules,
                existing.createdAt(),
                Instant.now(),
                existing.createdBy(),
                request.updatedBy()
        );
        
        RuleSet saved = ruleSetService.update(tenantId, ruleSetCode, version, updated);
        return ResponseEntity.ok(dtoMapper.toDto(saved));
    }
    
    @DeleteMapping("/{ruleSetCode}/versions/{version}")
    public ResponseEntity<Void> delete(
            @PathVariable String tenantId,
            @PathVariable String ruleSetCode,
            @PathVariable String version
    ) {
        ruleSetService.delete(tenantId, ruleSetCode, version);
        return ResponseEntity.noContent().build();
    }
}

