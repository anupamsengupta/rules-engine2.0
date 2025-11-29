package com.quickysoft.validationengine.test;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.model.ValidationContext;
import com.quickysoft.validationengine.model.*;
import org.junit.jupiter.api.Test;
import org.mvel2.MVEL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShoppingCartMvelValidationTest {

    /**
     * MVEL expression that validates cart total equals sum of line item totals.
     * Each line item total is calculated as: quantity * product.price
     */
    private static final String CART_TOTAL_EXPRESSION =
            "sum = new java.math.BigDecimal(\"0\"); " +
                    "foreach (li : context.shoppingCart.lineItems) { " +
                    "  sum = sum.add(li.product.price.multiply(new java.math.BigDecimal(li.quantity))); " +
                    "} " +
                    "context.shoppingCart.cartTotalAmount.compareTo(sum) == 0";
    
    /**
     * MVEL expression that validates cart total does not exceed user's limit.
     * Returns true if cartTotalAmount <= user.limit
     */
    private static final String USER_LIMIT_EXPRESSION =
            "context.shoppingCart.cartTotalAmount.compareTo(context.shoppingCart.user.limit) <= 0";



    @Test
    void testCartTotalMatchesLineItems_positive() throws Exception {
        // Build shopping cart with multiple line items
        ShoppingCart cart = createShoppingCart();
        
        // Calculate correct total: sum of (quantity * product.price) for all line items
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (LineItem item : cart.getLineItems()) {
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            expectedTotal = expectedTotal.add(itemTotal);
        }
        
        // Set cartTotalAmount to the computed sum
        cart.setCartTotalAmount(expectedTotal);
        
        // Prepare MVEL context
        Map<String, Object> variables = new HashMap<>();
        variables.put("shoppingCart", cart);
        
        // Evaluate MVEL expression
        ValidationContext ctx = new ValidationContext("TEST Total Expression", variables);
        Object result = ExpressionEvaluatorFactory
                .getInstance()
                .getEvaluator(ExpressionEvaluatorType.MVEL)
                .evaluate(CART_TOTAL_EXPRESSION, ctx);
        
        // Assert true - cart total matches line items sum
        assertTrue(result instanceof Boolean && (Boolean) result,
                "Cart total should match the sum of line item totals");
    }

    @Test
    void testCartTotalMatchesLineItems_negative() throws Exception {
        // Build shopping cart with multiple line items
        ShoppingCart cart = createShoppingCart();
        
        // Calculate correct total
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (LineItem item : cart.getLineItems()) {
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            expectedTotal = expectedTotal.add(itemTotal);
        }
        
        // Set cartTotalAmount to an incorrect value (add 10.00)
        cart.setCartTotalAmount(expectedTotal.add(new BigDecimal("10.00")));
        
        // Prepare MVEL context
        Map<String, Object> variables = new HashMap<>();
        variables.put("shoppingCart", cart);
        
        // Evaluate MVEL expression
        ValidationContext ctx = new ValidationContext("TEST Total Expression", variables);
        Object result = ExpressionEvaluatorFactory
                .getInstance()
                .getEvaluator(ExpressionEvaluatorType.MVEL)
                .evaluate(CART_TOTAL_EXPRESSION, ctx);
        
        // Assert false - cart total does not match line items sum
        assertFalse(result instanceof Boolean && (Boolean) result,
                "Cart total should not match the sum of line item totals when incorrect");
    }

    @Test
    void testCartTotalWithinUserLimit_positive() throws Exception {
        // Build shopping cart with user limit of 5000.00
        ShoppingCart cart = createShoppingCart();
        
        // Set cart total to a value within user limit (e.g., 2000.00)
        BigDecimal cartTotal = new BigDecimal("2000.00");
        cart.setCartTotalAmount(cartTotal);
        
        // Prepare MVEL context
        Map<String, Object> variables = new HashMap<>();
        variables.put("shoppingCart", cart);
        
        // Evaluate MVEL expression
        ValidationContext ctx = new ValidationContext("TEST limit Expression", variables);
        Object result = ExpressionEvaluatorFactory
                .getInstance()
                .getEvaluator(ExpressionEvaluatorType.MVEL)
                .evaluate(USER_LIMIT_EXPRESSION, ctx);
        
        // Assert true - cart total is within user limit
        assertTrue(result instanceof Boolean && (Boolean) result,
                "Cart total should be within user limit");
    }

    @Test
    void testCartTotalWithinUserLimit_boundary() throws Exception {
        // Build shopping cart with user limit of 5000.00
        ShoppingCart cart = createShoppingCart();
        
        // Set cart total to exactly the user limit (boundary case)
        BigDecimal cartTotal = cart.getUser().getLimit();
        cart.setCartTotalAmount(cartTotal);
        
        // Prepare MVEL context
        Map<String, Object> variables = new HashMap<>();
        variables.put("shoppingCart", cart);
        
        // Evaluate MVEL expression
        ValidationContext ctx = new ValidationContext("TEST limit Expression", variables);
        Object result = ExpressionEvaluatorFactory
                .getInstance()
                .getEvaluator(ExpressionEvaluatorType.MVEL)
                .evaluate(USER_LIMIT_EXPRESSION, ctx);
        
        // Assert true - cart total equals user limit (should pass with <=)
        assertTrue(result instanceof Boolean && (Boolean) result,
                "Cart total equal to user limit should be valid");
    }

    @Test
    void testCartTotalExceedsUserLimit_negative() throws Exception {
        // Build shopping cart with user limit of 5000.00
        ShoppingCart cart = createShoppingCart();
        
        // Set cart total to a value exceeding user limit (e.g., 6000.00)
        BigDecimal cartTotal = new BigDecimal("6000.00");
        cart.setCartTotalAmount(cartTotal);
        
        // Prepare MVEL context
        Map<String, Object> variables = new HashMap<>();
        variables.put("shoppingCart", cart);
        
        // Evaluate MVEL expression
        ValidationContext ctx = new ValidationContext("TEST limit Expression", variables);
        Object result = ExpressionEvaluatorFactory
                .getInstance()
                .getEvaluator(ExpressionEvaluatorType.MVEL)
                .evaluate(USER_LIMIT_EXPRESSION, ctx);
        
        // Assert false - cart total exceeds user limit
        assertFalse(result instanceof Boolean && (Boolean) result,
                "Cart total should not exceed user limit");
    }

    /**
     * Helper method to create a shopping cart with test data.
     */
    private ShoppingCart createShoppingCart() {
        // Create products
        Product laptop = new Product("P001", "Laptop", ProductCategory.ELECTRONICS, 
                new BigDecimal("999.99"));
        Product mouse = new Product("P002", "Wireless Mouse", ProductCategory.ELECTRONICS, 
                new BigDecimal("29.99"));
        Product coffee = new Product("P003", "Coffee Beans", ProductCategory.CONSUMABLES, 
                new BigDecimal("12.50"));
        
        // Create line items
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(new LineItem("LI001", laptop, 1, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI002", mouse, 2, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI003", coffee, 3, BigDecimal.ZERO));
        
        // Create user
        User user = new User("LOGIN001", "USER001", new BigDecimal("5000.00"), 
                UserStatus.ACTIVE, "user@example.com");
        
        // Create shopping cart (cartTotalAmount will be set in tests)
        return new ShoppingCart(lineItems, user, BigDecimal.ZERO);
    }
}
