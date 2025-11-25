package com.quickysoft.validationengine.core.evaluation;

/**
 * Overall status of a rule set evaluation.
 */
public enum RuleSetEvaluationStatus {
    /**
     * All rules passed.
     */
    PASS,
    
    /**
     * One or more rules failed.
     */
    FAIL,
    
    /**
     * One or more rules produced warnings (non-blocking failures).
     */
    WARN
}

