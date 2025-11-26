package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluationException;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.model.ValidationContext;
import org.mvel2.MVEL;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExecutableAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * MVEL (MVFLEX Expression Language) implementation of ExpressionEvaluator.
 */
public class MVELExpressionEvaluator extends AbstractExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(MVELExpressionEvaluator.class);

    MVELExpressionEvaluator() {}
    
    @Override
    public boolean evaluate(String expression, ValidationContext<?> context) throws ExpressionEvaluationException {
        try {
            logger.debug("Evaluating MVEL expression: {}", expression);

            // Create variable map
            Map<String, Object> variables = new HashMap<>();
            variables.put("payload", context.payload());
            // Build and set nested context for hierarchical attributes
            Map<String, Object> nestedContext = buildNestedContext(context.contextAttributes());
            variables.put("context", nestedContext);

            // Set tenantId if available
            if (context.contextAttributes().containsKey("tenantId")) {
                variables.put("tenantId", context.contextAttributes().get("tenantId"));
            }

            // Compile and evaluate the expression
            ExecutableAccessor compiled = (ExecutableAccessor) MVEL.compileExpression(expression);

            // Evaluate expression
            Object result = MVEL.executeExpression(compiled, variables);
            
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
            logger.error("Error evaluating MVEL expression: {}", expression, e);
            throw new ExpressionEvaluationException(
                    "Failed to evaluate MVEL expression: " + expression, e
            );
        }
    }
    
    @Override
    public String getName() {
        return "MVEL";
    }
}

