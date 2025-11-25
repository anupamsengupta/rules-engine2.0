package com.quickysoft.validation.admin.api.controller;

import com.quickysoft.validation.admin.api.dto.RuleRequest;
import com.quickysoft.validation.admin.api.dto.RuleResponse;
import com.quickysoft.validation.admin.api.mapper.RuleSetDtoMapper;
import com.quickysoft.validation.admin.api.service.RuleAdminService;
import com.quickysoft.validation.core.model.Rule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing rules within rule sets.
 */
@RestController
@RequestMapping("/tenants/{tenantId}/rulesets/{ruleSetCode}/versions/{version}/rules")
@Tag(name = "Rules", description = "API for managing rules within rule sets")
public class RuleController {
    
    private final RuleAdminService service;
    private final RuleSetDtoMapper mapper;
    
    public RuleController(RuleAdminService service, RuleSetDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @PostMapping
    @Operation(summary = "Add a rule", description = "Adds a new rule to a rule set")
    public ResponseEntity<RuleResponse> addRule(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String ruleSetCode,
            @Parameter(description = "Version", required = true)
            @PathVariable String version,
            @Valid @RequestBody RuleRequest request
    ) {
        Rule created = service.addRule(tenantId, ruleSetCode, version, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }
    
    @PutMapping("/{ruleCode}")
    @Operation(summary = "Update a rule", description = "Updates an existing rule in a rule set")
    public ResponseEntity<RuleResponse> updateRule(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String ruleSetCode,
            @Parameter(description = "Version", required = true)
            @PathVariable String version,
            @Parameter(description = "Rule code", required = true)
            @PathVariable String ruleCode,
            @Valid @RequestBody RuleRequest request
    ) {
        Rule updated = service.updateRule(tenantId, ruleSetCode, version, ruleCode, request);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
    
    @DeleteMapping("/{ruleCode}")
    @Operation(summary = "Delete a rule", description = "Deactivates a rule (soft delete)")
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String ruleSetCode,
            @Parameter(description = "Version", required = true)
            @PathVariable String version,
            @Parameter(description = "Rule code", required = true)
            @PathVariable String ruleCode
    ) {
        service.deleteRule(tenantId, ruleSetCode, version, ruleCode);
        return ResponseEntity.noContent().build();
    }
}

