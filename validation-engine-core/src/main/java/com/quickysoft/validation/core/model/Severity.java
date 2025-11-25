package com.quickysoft.validation.core.model;

/**
 * Severity level for a rule.
 * Determines how rule failures are treated in the overall rule set evaluation.
 */
public enum Severity {
    /**
     * Informational - does not affect overall status.
     */
    INFO,
    
    /**
     * Warning - may affect overall status depending on configuration.
     */
    WARN,
    
    /**
     * Error - causes rule set to fail.
     */
    ERROR
}

