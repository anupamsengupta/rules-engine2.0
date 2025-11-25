package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.RuleSetResult;
import com.quickysoft.validation.core.model.RuleSetStatus;
import com.quickysoft.validation.core.model.RuleStatus;
import com.quickysoft.validation.core.model.Severity;

import java.util.List;
import java.util.UUID;

/**
 * Calculates the overall rule set status from individual rule results.
 */
public class RuleSetResultCalculator {
    
    /**
     * Calculates the overall status of a rule set evaluation based on rule results.
     * 
     * Rules with ERROR severity that fail cause overall status to be FAIL.
     * Rules with WARN severity that fail cause overall status to be WARN (if no ERROR failures).
     * Rules with INFO severity that fail do not affect overall status.
     * Any rule with ERROR status causes overall status to be ERROR.
     * 
     * @param ruleSetId the rule set identifier
     * @param tenantId the tenant identifier
     * @param ruleSetCode the rule set code
     * @param ruleSetVersion the rule set version
     * @param ruleResults the individual rule results
     * @return the calculated rule set result
     */
    public RuleSetResult calculate(
            UUID ruleSetId,
            String tenantId,
            String ruleSetCode,
            String ruleSetVersion,
            List<RuleResult> ruleResults
    ) {
        if (ruleResults == null || ruleResults.isEmpty()) {
            return RuleSetResult.builder()
                    .ruleSetId(ruleSetId)
                    .tenantId(tenantId)
                    .ruleSetCode(ruleSetCode)
                    .ruleSetVersion(ruleSetVersion)
                    .overallStatus(RuleSetStatus.PASS)
                    .ruleResults(List.of())
                    .build();
        }
        
        // Check for any ERROR status (execution errors)
        boolean hasError = ruleResults.stream()
                .anyMatch(result -> result.status() == RuleStatus.ERROR);
        
        if (hasError) {
            return RuleSetResult.builder()
                    .ruleSetId(ruleSetId)
                    .tenantId(tenantId)
                    .ruleSetCode(ruleSetCode)
                    .ruleSetVersion(ruleSetVersion)
                    .overallStatus(RuleSetStatus.ERROR)
                    .ruleResults(ruleResults)
                    .build();
        }
        
        // Check for ERROR severity failures
        boolean hasErrorSeverityFailure = ruleResults.stream()
                .anyMatch(result -> 
                        result.severity() == Severity.ERROR && 
                        result.status() == RuleStatus.FAILED
                );
        
        if (hasErrorSeverityFailure) {
            return RuleSetResult.builder()
                    .ruleSetId(ruleSetId)
                    .tenantId(tenantId)
                    .ruleSetCode(ruleSetCode)
                    .ruleSetVersion(ruleSetVersion)
                    .overallStatus(RuleSetStatus.FAIL)
                    .ruleResults(ruleResults)
                    .build();
        }
        
        // Check for WARN severity failures
        boolean hasWarnSeverityFailure = ruleResults.stream()
                .anyMatch(result -> 
                        result.severity() == Severity.WARN && 
                        result.status() == RuleStatus.FAILED
                );
        
        if (hasWarnSeverityFailure) {
            return RuleSetResult.builder()
                    .ruleSetId(ruleSetId)
                    .tenantId(tenantId)
                    .ruleSetCode(ruleSetCode)
                    .ruleSetVersion(ruleSetVersion)
                    .overallStatus(RuleSetStatus.WARN)
                    .ruleResults(ruleResults)
                    .build();
        }
        
        // All passed or only INFO failures
        return RuleSetResult.builder()
                .ruleSetId(ruleSetId)
                .tenantId(tenantId)
                .ruleSetCode(ruleSetCode)
                .ruleSetVersion(ruleSetVersion)
                .overallStatus(RuleSetStatus.PASS)
                .ruleResults(ruleResults)
                .build();
    }
}

