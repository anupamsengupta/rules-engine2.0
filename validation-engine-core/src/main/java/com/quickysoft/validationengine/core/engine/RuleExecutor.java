package com.quickysoft.validationengine.core.engine;

import com.quickysoft.validationengine.core.domain.Rule;
import com.quickysoft.validationengine.core.domain.ValidationContext;
import com.quickysoft.validationengine.core.evaluation.RuleEvaluationResult;

/**
 * Executes a single rule against a validation context.
 */
public interface RuleExecutor {
    
    /**
     * Evaluates a rule against the given context.
     *
     * @param rule the rule to evaluate
     * @param context the validation context
     * @return the evaluation result
     * @throws RuleExecutionException if execution fails
     */
    RuleEvaluationResult execute(Rule rule, ValidationContext context) throws RuleExecutionException;
    
    /**
     * Checks if this executor supports the given rule type.
     *
     * @param rule the rule to check
     * @return true if this executor can handle the rule
     */
    boolean supports(Rule rule);
}

