package com.quickysoft.validationengine.test;

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
    /*private static final String EXPRESSION =
            "shoppingCart.cartTotalAmount.compareTo(" +
                    "  shoppingCart.lineItems.{ this.product.price.multiply(new java.math.BigDecimal(this.quantity)) }.sum()" +
                    ") == 0";*/
    String EXPRESSION =
            "sum = new java.math.BigDecimal(\"0\"); " +
                    "foreach (li : shoppingCart.lineItems) { " +
                    "  sum = sum.add(li.product.price.multiply(new java.math.BigDecimal(li.quantity))); " +
                    "} " +
                    "shoppingCart.cartTotalAmount.compareTo(sum) == 0";



    @Test
    void testCartTotalMatchesLineItems_positive() {
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
        Object result = MVEL.eval(EXPRESSION, variables);
        
        // Assert true - cart total matches line items sum
        assertTrue(result instanceof Boolean && (Boolean) result,
                "Cart total should match the sum of line item totals");
    }

    @Test
    void testCartTotalMatchesLineItems_negative() {
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
        Object result = MVEL.eval(EXPRESSION, variables);
        
        // Assert false - cart total does not match line items sum
        assertFalse(result instanceof Boolean && (Boolean) result,
                "Cart total should not match the sum of line item totals when incorrect");
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
