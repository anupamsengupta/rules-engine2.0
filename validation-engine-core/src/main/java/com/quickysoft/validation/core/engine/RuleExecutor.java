package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.ValidationContext;

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
    RuleResult execute(Rule rule, ValidationContext<?> context) throws RuleExecutionException;
    
    /**
     * Checks if this executor supports the given rule type.
     *
     * @param rule the rule to check
     * @return true if this executor can handle the rule
     */
    boolean supports(Rule rule);
}

