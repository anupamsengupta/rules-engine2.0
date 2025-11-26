package com.quickysoft.validation.core.engine.expression;

/**
 * Type of expression evaluator to use.
 */
public enum ExpressionEvaluatorType {
    /**
     * Spring Expression Language (SpEL) - default.
     */
    SPEL,
    
    /**
     * MVFLEX Expression Language (MVEL).
     */
    MVEL,
    
    /**
     * Java Expression Language (JEXL).
     */
    JEXL;
    
    /**
     * Returns the ExpressionEvaluatorType enum value based on a string.
     * The matching is case-insensitive.
     * 
     * @param value the string value to match (e.g., "SPEL", "spel", "MVEL", "mvel", "JEXL", "jexl")
     * @return the matching ExpressionEvaluatorType, or SPEL as default if value is null, empty, or doesn't match
     */
    public static ExpressionEvaluatorType fromString(String value) {
        if (value == null || value.isBlank()) {
            return SPEL; // Default
        }
        
        String normalized = value.trim().toUpperCase();
        
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // If the value doesn't match any enum, return default
            return SPEL;
        }
    }
    
    /**
     * Returns the ExpressionEvaluatorType enum value based on a string.
     * The matching is case-insensitive.
     * 
     * @param value the string value to match (e.g., "SPEL", "spel", "MVEL", "mvel", "JEXL", "jexl")
     * @param defaultValue the default value to return if value is null, empty, or doesn't match
     * @return the matching ExpressionEvaluatorType, or the provided default if value is null, empty, or doesn't match
     */
    public static ExpressionEvaluatorType fromString(String value, ExpressionEvaluatorType defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue != null ? defaultValue : SPEL;
        }
        
        String normalized = value.trim().toUpperCase();
        
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // If the value doesn't match any enum, return default
            return defaultValue != null ? defaultValue : SPEL;
        }
    }
}

