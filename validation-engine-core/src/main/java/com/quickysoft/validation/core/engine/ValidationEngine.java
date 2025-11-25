package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.RuleSet;
import com.quickysoft.validation.core.model.RuleSetResult;
import com.quickysoft.validation.core.model.ValidationContext;

import java.util.Map;

/**
 * Main API for the validation engine.
 * 
 * Provides a clean Java API for evaluating rule sets against validation contexts.
 * This is the primary interface that Spring Boot applications will use.
 */
public interface ValidationEngine {
    
    /**
     * Evaluates a rule set against a payload and context attributes.
     * Uses the latest active version of the rule set.
     * 
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param payload the object being validated
     * @param contextAttributes additional context attributes (userId, country, channel, etc.)
     * @param <T> the type of the payload
     * @return the evaluation result
     * @throws RuleExecutionException if evaluation fails
     */
    <T> RuleSetResult evaluate(
            String tenantId,
            String ruleSetCode,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException;
    
    /**
     * Evaluates a rule set against a payload and context attributes with a specific version.
     * 
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the rule set version
     * @param payload the object being validated
     * @param contextAttributes additional context attributes
     * @param <T> the type of the payload
     * @return the evaluation result
     * @throws RuleExecutionException if evaluation fails
     */
    <T> RuleSetResult evaluate(
            String tenantId,
            String ruleSetCode,
            String version,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException;
    
    /**
     * Evaluates a rule set directly (bypassing provider lookup).
     * 
     * @param ruleSet the rule set to evaluate
     * @param payload the object being validated
     * @param contextAttributes additional context attributes
     * @param <T> the type of the payload
     * @return the evaluation result
     * @throws RuleExecutionException if evaluation fails
     */
    <T> RuleSetResult evaluate(
            RuleSet ruleSet,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException;
}

