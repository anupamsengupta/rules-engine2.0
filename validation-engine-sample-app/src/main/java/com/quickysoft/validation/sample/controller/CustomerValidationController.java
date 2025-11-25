package com.quickysoft.validation.sample.controller;

import com.quickysoft.validation.core.model.RuleSetResult;
import com.quickysoft.validation.sample.domain.Customer;
import com.quickysoft.validation.sample.service.CustomerValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for customer validation.
 */
@RestController
@RequestMapping("/tenants/{tenantId}/validate-customer")
@Tag(name = "Customer Validation", description = "Customer validation using the validation engine")
public class CustomerValidationController {
    
    private final CustomerValidationService validationService;
    
    public CustomerValidationController(CustomerValidationService validationService) {
        this.validationService = validationService;
    }
    
    @PostMapping
    @Operation(summary = "Validate customer", 
               description = "Validates a customer using the validation engine for the specified tenant")
    public ResponseEntity<RuleSetResult> validateCustomer(
            @Parameter(description = "Tenant identifier", required = true)
            @PathVariable String tenantId,
            @RequestBody Customer customer,
            @RequestParam(required = false) Map<String, Object> context
    ) {
        // Default context if not provided
        if (context == null) {
            context = Map.of("channel", customer.channel() != null ? customer.channel() : "WEB");
        }
        
        RuleSetResult result = validationService.validateCustomer(tenantId, customer, context);
        return ResponseEntity.ok(result);
    }
}

