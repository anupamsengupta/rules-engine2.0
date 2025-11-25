package com.quickysoft.validation.admin.api.controller;

import com.quickysoft.validation.admin.api.dto.RuleSetRequest;
import com.quickysoft.validation.admin.api.dto.RuleSetResponse;
import com.quickysoft.validation.admin.api.mapper.RuleSetDtoMapper;
import com.quickysoft.validation.admin.api.service.RuleSetAdminService;
import com.quickysoft.validation.core.model.RuleSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing rule sets.
 */
@RestController
@RequestMapping("/tenants/{tenantId}/rulesets")
@Tag(name = "Rule Sets", description = "API for managing rule sets")
public class RuleSetController {
    
    private final RuleSetAdminService service;
    private final RuleSetDtoMapper mapper;
    
    public RuleSetController(RuleSetAdminService service, RuleSetDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @PostMapping
    @Operation(summary = "Create a rule set", description = "Creates a new rule set for the specified tenant")
    public ResponseEntity<RuleSetResponse> create(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Valid @RequestBody RuleSetRequest request
    ) {
        RuleSet created = service.create(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }
    
    @GetMapping
    @Operation(summary = "List rule sets", description = "Lists all rule sets for the specified tenant with pagination")
    public ResponseEntity<Page<RuleSetResponse>> list(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            Pageable pageable
    ) {
        Page<RuleSet> ruleSets = service.getAll(tenantId, pageable);
        return ResponseEntity.ok(ruleSets.map(mapper::toResponse));
    }
    
    @GetMapping("/{code}")
    @Operation(summary = "Get rule set versions", description = "Gets all versions of a rule set for the specified tenant")
    public ResponseEntity<List<RuleSetResponse>> getByCode(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String code
    ) {
        List<RuleSet> ruleSets = service.getByCode(tenantId, code);
        return ResponseEntity.ok(ruleSets.stream().map(mapper::toResponse).toList());
    }
    
    @GetMapping("/{code}/versions/{version}")
    @Operation(summary = "Get rule set by version", description = "Gets a specific version of a rule set")
    public ResponseEntity<RuleSetResponse> getByCodeAndVersion(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String code,
            @Parameter(description = "Version", required = true)
            @PathVariable String version
    ) {
        RuleSet ruleSet = service.getByCodeAndVersion(tenantId, code, version);
        return ResponseEntity.ok(mapper.toResponse(ruleSet));
    }
    
    @PutMapping("/{code}/versions/{version}")
    @Operation(summary = "Update rule set", description = "Updates or creates a rule set (upsert)")
    public ResponseEntity<RuleSetResponse> update(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String code,
            @Parameter(description = "Version", required = true)
            @PathVariable String version,
            @Valid @RequestBody RuleSetRequest request
    ) {
        RuleSet updated = service.update(tenantId, code, version, request);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
    
    @DeleteMapping("/{code}/versions/{version}")
    @Operation(summary = "Delete rule set", description = "Deactivates a rule set (soft delete)")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @Parameter(description = "Rule set code", required = true)
            @PathVariable String code,
            @Parameter(description = "Version", required = true)
            @PathVariable String version
    ) {
        service.delete(tenantId, code, version);
        return ResponseEntity.noContent().build();
    }
}

