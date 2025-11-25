package com.quickysoft.validation.sample.service;

import com.quickysoft.validation.core.engine.RuleExecutionException;
import com.quickysoft.validation.core.engine.ValidationEngine;
import com.quickysoft.validation.core.model.RuleSetResult;
import com.quickysoft.validation.sample.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service demonstrating validation engine usage.
 */
@Service
public class CustomerValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerValidationService.class);
    
    private final ValidationEngine validationEngine;
    
    public CustomerValidationService(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }
    
    /**
     * Validates a customer using the validation engine.
     * 
     * @param tenantId the tenant identifier
     * @param customer the customer to validate
     * @param contextAttributes additional context (channel, market, etc.)
     * @return the validation result
     */
    public RuleSetResult validateCustomer(
            String tenantId,
            Customer customer,
            Map<String, Object> contextAttributes
    ) {
        try {
            logger.info("Validating customer {} for tenant {}", customer.id(), tenantId);
            
            // Call the validation engine
            // Note: This assumes a rule set "customer-onboarding" exists for the tenant
            RuleSetResult result = validationEngine.evaluate(
                    tenantId,
                    "customer-onboarding",
                    "1.0", // version - in production, you might want to get latest active version
                    customer,
                    contextAttributes
            );
            
            logger.info("Validation completed for customer {}: status={}, failedRules={}",
                    customer.id(), result.overallStatus(), result.getFailedRuleCount());
            
            return result;
        } catch (RuleExecutionException e) {
            logger.error("Error during validation for customer {}", customer.id(), e);
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        }
    }
}

