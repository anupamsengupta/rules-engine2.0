package com.quickysoft.validation.core.model;

/**
 * Status of a rule evaluation.
 */
public enum RuleStatus {
    /**
     * Rule passed validation.
     */
    PASSED,
    
    /**
     * Rule failed validation.
     */
    FAILED,
    
    /**
     * Rule was skipped (e.g., not applicable to context).
     */
    SKIPPED,
    
    /**
     * Error occurred during rule evaluation.
     */
    ERROR
}

