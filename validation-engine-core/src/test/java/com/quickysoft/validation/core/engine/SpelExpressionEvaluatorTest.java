package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.ExpressionRuleExecutor;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.engine.expression.impl.SpELExpressionEvaluator;
import com.quickysoft.validation.core.model.ExpressionRule;
import com.quickysoft.validation.core.model.RuleResult;
import com.quickysoft.validation.core.model.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


class SpelExpressionEvaluatorTest {

    @Test
    void testHierarchicalAttributesEvaluation() throws Exception {

        Map<String, Object> attrs = new HashMap<>();
        // Hierarchical key: "order.currency" -> "USD" (dot notation builds nested map: context.order.currency = "USD")
        attrs.put("order.currency", "USD");
        // Hierarchical key: "product.uom" -> "BBL" (dot notation builds nested map: context.product.uom = "BBL")
        attrs.put("product.uom", "BBL");
        // Flat attribute for comparison
        attrs.put("userRole", "ADMIN");

        // Setup ValidationContext with hierarchical attributes
        ValidationContext ctx = new ValidationContext("Oil transaction in barrels", attrs);

        ExpressionRuleExecutor ruleExecutor = new ExpressionRuleExecutor(ExpressionEvaluatorFactory.getInstance().getEvaluator(ExpressionEvaluatorType.SPEL));
        ExpressionRule rule1 = ExpressionRule
                .builder()
                .tenantId("T1")
                .ruleCode("R1")
                .name("R1")
                .expression("#payload.toLowerCase().contains('oil') and " +
                        "(#context.order.currency == 'USD' or #context.product.uom == 'BBL')")
                .build();

        // Test 1: AND with OR on hierarchical attributes
        Object result = ruleExecutor.execute(
                rule1,
                ctx
        );
        assertTrue(((RuleResult)result).isSuccess(), "Expected true for payload containing 'oil' AND (currency USD OR UOM BBL)");

        ExpressionRule rule2 = ExpressionRule
                .builder()
                .tenantId("T1")
                .ruleCode("R2")
                .name("R2")
                .expression("#payload.toLowerCase().contains('error') or " +
                        "not (#context.order.currency == 'EUR' and #context.product.uom == 'KG')")
                .build();

        // Test 2: OR with negation on hierarchical attributes (corrected expectation: true)
        // Payload does not contain 'error' (false), but not (currency == 'EUR' and uom == 'KG') = not (false and false) = true
        // Overall: false OR true = true
        Object result2 = ruleExecutor.execute(
                rule2,
                ctx
        );
        assertTrue(((RuleResult)result2).isSuccess(), "Expected true for no 'error' in payload OR not (currency EUR AND UOM KG)");
    }
}
