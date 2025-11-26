package com.quickysoft.validation.core.engine.expression;

import com.quickysoft.validation.core.model.ValidationContext;

/**
 * Strategy interface for evaluating expressions.
 * 
 * Allows switching between different expression evaluation engines
 * (SpEL, MVEL, JEXL) based on configuration.
 */
public interface ExpressionEvaluator {
    
    /**
     * Evaluates a boolean expression against a validation context.
     * 
     * @param expression the expression to evaluate
     * @param context the validation context
     * @return true if the expression evaluates to true, false otherwise
     * @throws ExpressionEvaluationException if evaluation fails
     */
    boolean evaluate(String expression, ValidationContext<?> context) throws ExpressionEvaluationException;
    
    /**
     * Gets the name of this evaluator (e.g., "SPEL", "MVEL", "JEXL").
     */
    String getName();
}

