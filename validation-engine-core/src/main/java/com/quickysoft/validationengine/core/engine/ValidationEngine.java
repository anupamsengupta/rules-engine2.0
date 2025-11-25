package com.quickysoft.validationengine.core.engine;

import com.quickysoft.validationengine.core.domain.RuleSet;
import com.quickysoft.validationengine.core.domain.ValidationContext;
import com.quickysoft.validationengine.core.evaluation.RuleSetEvaluationResult;

/**
 * Main API for the validation engine.
 * Evaluates rule sets against validation contexts.
 */
public interface ValidationEngine {
    
    /**
     * Evaluates a rule set against the given context.
     *
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param version the version
     * @param context the validation context
     * @return the evaluation result
     * @throws RuleExecutionException if evaluation fails
     */
    RuleSetEvaluationResult evaluate(
            String tenantId,
            String ruleSetCode,
            String version,
            ValidationContext context
    ) throws RuleExecutionException;
    
    /**
     * Evaluates a rule set directly (bypassing provider lookup).
     *
     * @param ruleSet the rule set to evaluate
     * @param context the validation context
     * @return the evaluation result
     * @throws RuleExecutionException if evaluation fails
     */
    RuleSetEvaluationResult evaluate(RuleSet ruleSet, ValidationContext context) throws RuleExecutionException;
}

