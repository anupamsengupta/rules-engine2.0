package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluationException;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.model.ValidationContext;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlScript;
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
            
            // Try to create and evaluate as script first (for multi-statement expressions),
            // then fall back to expression if it fails
            Object result;
            // Check if expression contains multiple statements (semicolons or control structures)
            // JEXL scripts can have: for loops (with ':' or 'in'), foreach, if statements, semicolons
            boolean isScript = expression.contains(";") || 
                              expression.contains("for ") || 
                              expression.contains("foreach ") || 
                              expression.contains("if ") ||
                              expression.contains(" while ");
            
            if (isScript) {
                // Try to create and evaluate as script
                try {
                    JexlScript jexlScript = jexlEngine.createScript(expression);
                    result = jexlScript.execute(jexlContext);
                } catch (Exception scriptException) {
                    logger.debug("Failed to create JEXL script, trying as expression: {}", scriptException.getMessage());
                    // If script creation fails, try as expression (for backward compatibility)
                    try {
                        JexlExpression jexlExpression = jexlEngine.createExpression(expression);
                        result = jexlExpression.evaluate(jexlContext);
                    } catch (Exception exprException) {
                        // Re-throw the original script exception as it's more informative
                        throw scriptException;
                    }
                }
            } else {
                // Create and evaluate as expression
                JexlExpression jexlExpression = jexlEngine.createExpression(expression);
                result = jexlExpression.evaluate(jexlContext);
            }
            
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

