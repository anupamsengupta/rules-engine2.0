package com.quickysoft.validation.core.engine.expression;

/**
 * Exception thrown when expression evaluation fails.
 */
public class ExpressionEvaluationException extends Exception {
    
    public ExpressionEvaluationException(String message) {
        super(message);
    }
    
    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}

