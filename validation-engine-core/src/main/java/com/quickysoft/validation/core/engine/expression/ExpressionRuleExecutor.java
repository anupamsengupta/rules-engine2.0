package com.quickysoft.validation.core.engine.expression;

import com.quickysoft.validation.core.engine.RuleExecutionException;
import com.quickysoft.validation.core.engine.RuleExecutor;
import com.quickysoft.validation.core.model.ExpressionRule;
import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for expression-based rules.
 * 
 * Uses a strategy pattern to support multiple expression evaluation engines:
 * - SpEL (Spring Expression Language)
 * - MVEL (MVFLEX Expression Language)
 * - JEXL (Java Expression Language)
 * 
 * The evaluator is selected based on configuration.
 */
public class ExpressionRuleExecutor implements RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionRuleExecutor.class);
    
    private final ExpressionEvaluator expressionEvaluator;
    
    /**
     * Creates an ExpressionRuleExecutor with the specified evaluator.
     */
    public ExpressionRuleExecutor(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator == null) {
            throw new IllegalArgumentException("expressionEvaluator cannot be null");
        }
        this.expressionEvaluator = expressionEvaluator;
        logger.info("Initialized ExpressionRuleExecutor with {} evaluator", expressionEvaluator.getName());
    }
    
    @Override
    public RuleResult execute(Rule rule, ValidationContext<?> context) throws RuleExecutionException {
        if (!(rule instanceof ExpressionRule expressionRule)) {
            throw new IllegalArgumentException("Rule must be an ExpressionRule");
        }
        
        try {
            boolean result = expressionEvaluator.evaluate(expressionRule.expression(), context);
            
            if (result) {
                return RuleResult.passed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity()
                );
            } else {
                String message = formatFailureMessage(expressionRule, context);
                return RuleResult.failed(
                        rule.id(),
                        rule.tenantId(),
                        rule.ruleCode(),
                        rule.name(),
                        rule.severity(),
                        message
                );
            }
        } catch (ExpressionEvaluationException e) {
            logger.error("Error evaluating expression rule {} (evaluator: {}): {}", 
                    rule.ruleCode(), expressionEvaluator.getName(), e.getMessage(), e);
            return RuleResult.error(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    e
            );
        } catch (Exception e) {
            logger.error("Unexpected error evaluating expression rule {}: {}", rule.ruleCode(), e.getMessage(), e);
            return RuleResult.error(
                    rule.id(),
                    rule.tenantId(),
                    rule.ruleCode(),
                    rule.name(),
                    e
            );
        }
    }
    
    @Override
    public boolean supports(Rule rule) {
        return rule instanceof ExpressionRule;
    }
    
    /**
     * Formats the failure message, replacing placeholders if a template is provided.
     */
    private String formatFailureMessage(ExpressionRule rule, ValidationContext<?> context) {
        if (rule.failureMessageTemplate() != null && !rule.failureMessageTemplate().isBlank()) {
            // TODO: Replace placeholders in template (e.g., ${ruleCode}, ${payload.field})
            return rule.failureMessageTemplate();
        }
        return String.format("Rule '%s' failed: %s", rule.name(), rule.expression());
    }
}
