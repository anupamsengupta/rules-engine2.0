package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluationException;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.impl.JEXLExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.impl.MVELExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.impl.SpELExpressionEvaluator;
import com.quickysoft.validation.core.model.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for expression evaluator strategy implementations.
 */
class ExpressionEvaluatorStrategyTest {
    
    private record Customer(String name, Integer age, String email) {
    }
    
    @Test
    void testSpELEvaluator() throws ExpressionEvaluationException {
        ExpressionEvaluator evaluator =
                ExpressionEvaluatorFactory.getInstance().getEvaluator(ExpressionEvaluatorType.SPEL);
        
        Customer customer = new Customer("John", 25, "john@example.com");
        ValidationContext<Customer> context = new ValidationContext<>(
                customer,
                Map.of("channel", "WEB", "country", "US")//context
        );
        
        // Test simple expression
        //boolean result = evaluator.evaluate("payload.age >= 18", context);
        boolean result = evaluator.evaluate("#context.channel == 'WEB'", context);
        assertThat(result).isTrue();
        
        // Test context variable
        result = evaluator.evaluate("#context.country == 'US'", context);
        assertThat(result).isTrue();
        
        // Test complex expression
        //result = evaluator.evaluate("payload.age >= 18 && context['channel'] == 'WEB'", context);
        //assertThat(result).isTrue();
        
        assertThat(evaluator.getName()).isEqualTo("SPEL");
    }
    
    @Test
    void testMVELEvaluator() throws ExpressionEvaluationException {
        ExpressionEvaluator evaluator =
                ExpressionEvaluatorFactory.getInstance().getEvaluator(ExpressionEvaluatorType.MVEL);
        
        Customer customer = new Customer("John", 25, "john@example.com");
        ValidationContext<Customer> context = new ValidationContext<>(
                customer,
                Map.of("channel", "WEB", "country", "US")
        );
        
        // Test simple expression
        //boolean result = evaluator.evaluate("payload.age >= 18", context);
        boolean result = evaluator.evaluate("context.channel == 'WEB'", context);
        assertThat(result).isTrue();
        
        // Test context variable
        result = evaluator.evaluate("context.country == 'US'", context);
        assertThat(result).isTrue();
        
        // Test complex expression
        //result = evaluator.evaluate("payload.age >= 18 && channel == 'WEB'", context);
        //assertThat(result).isTrue();
        
        assertThat(evaluator.getName()).isEqualTo("MVEL");
    }
    
    @Test
    void testJEXLEvaluator() throws ExpressionEvaluationException {
        ExpressionEvaluator evaluator =
                ExpressionEvaluatorFactory.getInstance().getEvaluator(ExpressionEvaluatorType.JEXL);


        Customer customer = new Customer("John", 25, "john@example.com");
        ValidationContext<Customer> context = new ValidationContext<>(
                customer,
                Map.of("channel", "WEB", "country", "US")
        );
        
        // Test simple expression
        //boolean result = evaluator.evaluate("payload.age >= 18", context);
        boolean result = evaluator.evaluate("context.channel == 'WEB'", context);
        assertThat(result).isTrue();
        
        // Test context variable
        result = evaluator.evaluate("context.country == 'US'", context);
        assertThat(result).isTrue();
        
        // Test complex expression
        //result = evaluator.evaluate("payload.age >= 18 && channel == 'WEB'", context);
        //assertThat(result).isTrue();
        
        assertThat(evaluator.getName()).isEqualTo("JEXL");
    }
    
    @Test
    void testEvaluatorFailure() {
        ExpressionEvaluator evaluator = ExpressionEvaluatorFactory.getInstance().getEvaluator(ExpressionEvaluatorType.SPEL);
        
        Customer customer = new Customer("John", 25, "john@example.com");
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Invalid expression should throw exception
        try {
            evaluator.evaluate("invalid syntax !!!", context);
            // Should not reach here
            assertThat(false).isTrue();
        } catch (ExpressionEvaluationException e) {
            assertThat(e.getMessage()).contains("Failed to evaluate");
        }
    }
}

