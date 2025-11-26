package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluationException;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.model.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * SpEL (Spring Expression Language) implementation of ExpressionEvaluator.
 */
public class SpELExpressionEvaluator extends AbstractExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(SpELExpressionEvaluator.class);
    
    private final ExpressionParser parser = new SpelExpressionParser();

    SpELExpressionEvaluator() {}
    
    @Override
    public boolean evaluate(String expression, ValidationContext<?> context) throws ExpressionEvaluationException {
        try {
            logger.debug("Evaluating SpEL expression: {}", expression);
            
            // Create evaluation context
            StandardEvaluationContext evalContext = new StandardEvaluationContext();
            // Enable dot notation access for Maps
            evalContext.addPropertyAccessor(new MapAccessor());
            
            // Set root object (payload)
            evalContext.setVariable("payload", context.payload());

            // Build and set nested context for hierarchical attributes
            Map<String, Object> nestedContext = buildNestedContext(context.contextAttributes());
            evalContext.setVariable("context", nestedContext);
            
            // Set tenantId if available
            evalContext.setVariable("tenantId", context.contextAttributes().get("tenantId"));
            
            // Parse and evaluate expression
            Expression expr = parser.parseExpression(expression);
            Object result = expr.getValue(evalContext);
            
            // Convert to boolean
            if (result instanceof Boolean bool) {
                return bool;
            }
            if (result == null) {
                return false;
            }
            
            // Try to convert
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            logger.error("Error evaluating SpEL expression: {}", expression, e);
            throw new ExpressionEvaluationException(
                    "Failed to evaluate SpEL expression: " + expression, e
            );
        }
    }
    
    @Override
    public String getName() {
        return "SPEL";
    }
}

