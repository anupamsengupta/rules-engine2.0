package com.validationengine.core.domain;

/**
 * Type of rule execution strategy.
 */
public enum RuleType {
    /**
     * Expression-based rule (boolean expression/DSL).
     */
    EXPRESSION,
    
    /**
     * Groovy script-based rule.
     */
    GROOVY_SCRIPT
}

