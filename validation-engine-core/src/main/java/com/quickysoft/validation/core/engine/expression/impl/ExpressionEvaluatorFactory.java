package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;

/**
 * Factory for creating ExpressionEvaluator instances based on evaluator type.
 */
public class ExpressionEvaluatorFactory {
    private static ExpressionEvaluatorFactory instance = new ExpressionEvaluatorFactory();

    private ExpressionEvaluatorFactory() {
    }

    public static ExpressionEvaluatorFactory getInstance() {
        return instance;
    }

    /**
     * Creates an ExpressionEvaluator based on the evaluator type string.
     * 
     * @param evaluatorType the evaluator type string (e.g., "SPEL", "MVEL", "JEXL")
     * @return the corresponding ExpressionEvaluator instance
     * @throws IllegalArgumentException if the evaluator type is not recognized
     */
    public ExpressionEvaluator getEvaluator(String evaluatorType) {
        ExpressionEvaluatorType type = ExpressionEvaluatorType.fromString(evaluatorType);
        
        return switch (type) {
            case SPEL -> new SpELExpressionEvaluator();
            case MVEL -> new MVELExpressionEvaluator();
            case JEXL -> new JEXLExpressionEvaluator();
        };
    }
    
    /**
     * Creates an ExpressionEvaluator based on the evaluator type enum.
     * 
     * @param evaluatorType the evaluator type enum
     * @return the corresponding ExpressionEvaluator instance
     */
    public ExpressionEvaluator getEvaluator(ExpressionEvaluatorType evaluatorType) {
        if (evaluatorType == null) {
            evaluatorType = ExpressionEvaluatorType.SPEL; // Default
        }
        
        return switch (evaluatorType) {
            case SPEL -> new SpELExpressionEvaluator();
            case MVEL -> new MVELExpressionEvaluator();
            case JEXL -> new JEXLExpressionEvaluator();
        };
    }
}
