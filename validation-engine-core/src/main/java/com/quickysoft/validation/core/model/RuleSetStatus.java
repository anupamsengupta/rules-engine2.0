package com.quickysoft.validation.core.model;

/**
 * Overall status of a rule set evaluation.
 */
public enum RuleSetStatus {
    /**
     * All rules passed.
     */
    PASS,
    
    /**
     * One or more rules produced warnings.
     */
    WARN,
    
    /**
     * One or more rules failed.
     */
    FAIL,
    
    /**
     * Error occurred during rule set evaluation.
     */
    ERROR
}

