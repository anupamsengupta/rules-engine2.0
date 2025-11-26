package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluationException;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.model.ValidationContext;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * JEXL (Java Expression Language) implementation of ExpressionEvaluator.
 */
public class JEXLExpressionEvaluator extends AbstractExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(JEXLExpressionEvaluator.class);
    
    private final JexlEngine jexlEngine;
    
    JEXLExpressionEvaluator() {
        this.jexlEngine = new Engine();
    }
    
    @Override
    public boolean evaluate(String expression, ValidationContext<?> context) throws ExpressionEvaluationException {
        try {
            logger.debug("Evaluating JEXL expression: {}", expression);
            
            // Create JEXL context
            JexlContext jexlContext = new MapContext();
            
            // Set payload
            jexlContext.set("payload", context.payload());
            
            // Build and set nested context for hierarchical attributes
            Map<String, Object> nestedContext = buildNestedContext(context.contextAttributes());
            jexlContext.set("context", nestedContext);
            
            // Also set context attributes directly for backward compatibility
            for (Map.Entry<String, Object> entry : context.contextAttributes().entrySet()) {
                jexlContext.set(entry.getKey(), entry.getValue());
            }
            
            // Set tenantId if available
            if (context.contextAttributes().containsKey("tenantId")) {
                jexlContext.set("tenantId", context.contextAttributes().get("tenantId"));
            }
            
            // Create and evaluate expression
            JexlExpression jexlExpression = jexlEngine.createExpression(expression);
            Object result = jexlExpression.evaluate(jexlContext);
            
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
            logger.error("Error evaluating JEXL expression: {}", expression, e);
            throw new ExpressionEvaluationException(
                    "Failed to evaluate JEXL expression: " + expression, e
            );
        }
    }
    
    @Override
    public String getName() {
        return "JEXL";
    }
}

