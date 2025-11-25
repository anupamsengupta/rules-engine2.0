package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleSet;
import com.quickysoft.validation.core.model.RuleSetResult;
import com.quickysoft.validation.core.model.ValidationContext;
import com.quickysoft.validation.core.provider.RuleSetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of ValidationEngine.
 * 
 * Orchestrates rule set evaluation by:
 * 1. Loading rule sets via RuleSetProvider
 * 2. Filtering applicable rules based on context
 * 3. Executing rules in priority order
 * 4. Calculating overall status
 */
public class DefaultValidationEngine implements ValidationEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultValidationEngine.class);
    
    private final RuleSetProvider ruleSetProvider;
    private final List<RuleExecutor> ruleExecutors;
    private final RuleSetResultCalculator resultCalculator;
    
    public DefaultValidationEngine(
            RuleSetProvider ruleSetProvider,
            List<RuleExecutor> ruleExecutors,
            RuleSetResultCalculator resultCalculator
    ) {
        this.ruleSetProvider = ruleSetProvider;
        this.ruleExecutors = ruleExecutors != null ? new ArrayList<>(ruleExecutors) : new ArrayList<>();
        this.resultCalculator = resultCalculator != null ? resultCalculator : new RuleSetResultCalculator();
    }
    
    @Override
    public <T> RuleSetResult evaluate(
            String tenantId,
            String ruleSetCode,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException {
        // Try to get latest version from provider
        // If provider doesn't support this, we'll need to add a method to get latest version
        // For now, this is a simplified implementation that requires version
        throw new RuleExecutionException(
                "Version must be specified. Use evaluate(tenantId, ruleSetCode, version, payload, contextAttributes). " +
                "To support latest version lookup, implement RuleSetProvider.getLatestActiveVersion(tenantId, ruleSetCode)"
        );
    }
    
    @Override
    public <T> RuleSetResult evaluate(
            String tenantId,
            String ruleSetCode,
            String version,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException {
        // Load rule set
        RuleSet ruleSet = ruleSetProvider.getRuleSet(tenantId, ruleSetCode, version);
        if (ruleSet == null) {
            throw new RuleExecutionException(
                    String.format("Rule set not found: tenantId=%s, code=%s, version=%s",
                            tenantId, ruleSetCode, version)
            );
        }
        
        if (!ruleSet.active()) {
            throw new RuleExecutionException(
                    String.format("Rule set is not active: tenantId=%s, code=%s, version=%s",
                            tenantId, ruleSetCode, version)
            );
        }
        
        // Evaluate with the loaded rule set
        return evaluate(ruleSet, payload, contextAttributes);
    }
    
    @Override
    public <T> RuleSetResult evaluate(
            RuleSet ruleSet,
            T payload,
            Map<String, Object> contextAttributes
    ) throws RuleExecutionException {
        logger.debug("Evaluating rule set {} for tenant {}", ruleSet.code(), ruleSet.tenantId());
        
        // Create validation context
        ValidationContext<T> context = new ValidationContext<>(payload, contextAttributes);
        
        // Filter and sort rules
        List<Rule> applicableRules = ruleSet.rules().stream()
                .filter(rule -> rule.enabled())
                .filter(rule -> isApplicable(rule, context))
                .sorted(Comparator.comparing(Rule::priority, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        if (applicableRules.isEmpty()) {
            logger.debug("No applicable rules found for rule set {}", ruleSet.code());
            return resultCalculator.calculate(
                    ruleSet.id(),
                    ruleSet.tenantId(),
                    ruleSet.code(),
                    ruleSet.version(),
                    List.of()
            );
        }
        
        // Execute rules
        List<com.quickysoft.validation.core.model.RuleResult> ruleResults = new ArrayList<>();
        for (Rule rule : applicableRules) {
            try {
                RuleExecutor executor = findExecutor(rule);
                if (executor == null) {
                    logger.warn("No executor found for rule type: {}", rule.getClass().getSimpleName());
                    ruleResults.add(com.quickysoft.validation.core.model.RuleResult.error(
                            rule.id(),
                            rule.tenantId(),
                            rule.ruleCode(),
                            rule.name(),
                            new RuleExecutionException("No executor found for rule type: " + rule.getClass().getSimpleName())
                    ));
                    continue;
                }
                
                // Pass ruleSetCode to executor if it's a GroovyScriptRuleExecutor
                com.quickysoft.validation.core.model.RuleResult result;
                if (executor instanceof GroovyScriptRuleExecutor groovyExecutor) {
                    result = groovyExecutor.execute(rule, context, ruleSet.code());
                } else {
                    result = executor.execute(rule, context);
                }
                ruleResults.add(result);
                
                logger.debug("Rule {} evaluated with status: {}", rule.ruleCode(), result.status());
            } catch (Exception e) {
                logger.error("Error executing rule {}: {}", rule.ruleCode(), e.getMessage(), e);
                ruleResults.add(com.quickysoft.validation.core.model.RuleResult.error(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        e
                ));
            }
        }
        
        // Calculate overall result
        return resultCalculator.calculate(
                ruleSet.id(),
                ruleSet.tenantId(),
                ruleSet.code(),
                ruleSet.version(),
                ruleResults
        );
    }
    
    /**
     * Checks if a rule is applicable to the given context.
     * 
     * A rule is applicable if:
     * - It has no applicable contexts (applies to all)
     * - Or at least one of its applicable contexts matches a context attribute
     */
    private boolean isApplicable(Rule rule, ValidationContext<?> context) {
        if (rule.applicableContexts() == null || rule.applicableContexts().isEmpty()) {
            return true; // No restrictions, applies to all contexts
        }
        
        // Check if any applicable context matches
        return rule.applicableContexts().stream()
                .anyMatch(tag -> context.hasAttribute(tag) || 
                               context.contextAttributes().keySet().contains(tag));
    }
    
    /**
     * Finds an executor that supports the given rule.
     */
    private RuleExecutor findExecutor(Rule rule) {
        return ruleExecutors.stream()
                .filter(executor -> executor.supports(rule))
                .findFirst()
                .orElse(null);
    }
}

