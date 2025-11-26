package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.ExpressionRule;
import com.quickysoft.validation.core.model.Rule;
import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.RuleStatus;
import com.quickysoft.validation.core.model.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for expression-based rules.
 * 
 * Evaluates boolean expressions (e.g., SpEL) against the validation context.
 * This is a simple implementation that uses basic expression evaluation.
 * For production use, consider integrating with Spring Expression Language (SpEL).
 */
public class ExpressionRuleExecutor implements RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionRuleExecutor.class);
    private final ExpressionParser parser = new SpelExpressionParser();
    
    @Override
    public RuleResult execute(Rule rule, ValidationContext<?> context) throws RuleExecutionException {
        if (!(rule instanceof ExpressionRule expressionRule)) {
            throw new IllegalArgumentException("Rule must be an ExpressionRule");
        }
        
        try {
            boolean result = evaluateExpression(expressionRule.expression(), context);
            
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
        } catch (Exception e) {
            logger.error("Error evaluating expression rule {}: {}", rule.ruleCode(), e.getMessage(), e);
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
     * Builds a nested Map from flat Map<String, Object> where keys with dot notation (e.g., "order.currency")
     * are resolved into nested structures (e.g., context.get("order").get("currency")).
     */
    private Map<String, Object> buildNestedContext(Map<String, Object> flatAttributes) {
        Map<String, Object> nested = new HashMap<>();
        if (flatAttributes != null) {
            for (Map.Entry<String, Object> entry : flatAttributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                setNestedValue(nested, key.split("\\."), value);
            }
        }
        return nested;
    }

    /**
     * Recursively sets a value in a nested Map using the path segments.
     */
    @SuppressWarnings("unchecked")
    private void setNestedValue(Map<String, Object> root, String[] path, Object value) {
        Map<String, Object> current = root;
        for (int i = 0; i < path.length - 1; i++) {
            String segment = path[i];
            if (!current.containsKey(segment)) {
                current.put(segment, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(segment);
        }
        current.put(path[path.length - 1], value);
    }
    
    /**
     * Evaluates an expression against the validation context.
     * 
     * This is a simplified implementation. For production, integrate with SpEL or another
     * expression evaluation library.
     * 
     * @param expression the expression to evaluate
     * @param context the validation context
     * @return true if the expression evaluates to true
     */
    private boolean evaluateExpression(String expression, ValidationContext<?> context) {
        
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        
        // Enable dot notation access for Maps
        evalContext.addPropertyAccessor(new MapAccessor());
        
        // Set the payload as a root object/variable named 'payload'
        evalContext.setVariable("payload", context.payload());
        
        // Build and set nested context for hierarchical attributes
        Map<String, Object> nestedContext = buildNestedContext(context.contextAttributes());
        evalContext.setVariable("context", nestedContext);
        
        Expression expr = parser.parseExpression(expression);
        return (Boolean)expr.getValue(evalContext);
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

