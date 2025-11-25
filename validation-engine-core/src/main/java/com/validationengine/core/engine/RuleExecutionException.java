package com.validationengine.core.engine;

/**
 * Exception thrown when rule execution fails.
 */
public class RuleExecutionException extends Exception {
    
    public RuleExecutionException(String message) {
        super(message);
    }
    
    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}

