package com.quickysoft.validation.core.engine.benchmark;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.model.ValidationContext;
import com.quickysoft.validationengine.model.*;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for line item verification expression evaluation across SpEL, MVEL, and JEXL.
 * 
 * Line item verification case: Calculate sum of line items and compare with cart total
 * Expression: Sum of (quantity * product.price) == cartTotalAmount
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class LineItemVerificationBenchmark {
    
    private ExpressionEvaluator spelEvaluator;
    private ExpressionEvaluator mvelEvaluator;
    private ExpressionEvaluator jexlEvaluator;
    
    private ValidationContext<Object> context;
    
    // SpEL expression - using Stream API for collection reduction
    // Note: SpEL doesn't have native foreach or sum() for BigDecimal, so we use Java Stream API
    private String spelExpression = 
            "#context.shoppingCart.cartTotalAmount.compareTo(" +
            "  T(java.util.stream.StreamSupport).stream(" +
            "    #context.shoppingCart.lineItems.spliterator(), false" +
            "  ).map(li -> li.product.price.multiply(new java.math.BigDecimal(li.quantity)))" +
            "  .reduce(new java.math.BigDecimal(0), (a, b) -> a.add(b))" +
            ") == 0";
    
    // MVEL expression - using foreach loop
    private String mvelExpression =
            "sum = new java.math.BigDecimal(\"0\"); " +
            "foreach (li : context.shoppingCart.lineItems) { " +
            "  sum = sum.add(li.product.price.multiply(new java.math.BigDecimal(li.quantity))); " +
            "} " +
            "context.shoppingCart.cartTotalAmount.compareTo(sum) == 0";
    
    // JEXL expression - using foreach loop
    private String jexlExpression =
            "sum = new java.math.BigDecimal(\"0\"); " +
            "for (li : context.shoppingCart.lineItems) { " +
            "  sum = sum.add(li.product.price.multiply(new java.math.BigDecimal(li.quantity))); " +
            "} " +
            "context.shoppingCart.cartTotalAmount.compareTo(sum) == 0";
    
    @Setup
    public void setup() {
        ExpressionEvaluatorFactory factory = ExpressionEvaluatorFactory.getInstance();
        spelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.SPEL);
        mvelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.MVEL);
        jexlEvaluator = factory.getEvaluator(ExpressionEvaluatorType.JEXL);
        
        // Create shopping cart with test data
        ShoppingCart cart = createShoppingCart();
        
        // Calculate correct total
        BigDecimal expectedTotal = BigDecimal.ZERO;
        for (LineItem item : cart.getLineItems()) {
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            expectedTotal = expectedTotal.add(itemTotal);
        }
        cart.setCartTotalAmount(expectedTotal);
        
        // Create validation context
        Map<String, Object> contextAttributes = new HashMap<>();
        contextAttributes.put("shoppingCart", cart);
        context = new ValidationContext<>("test-payload", contextAttributes);
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
        Product keyboard = new Product("P004", "Mechanical Keyboard", ProductCategory.ELECTRONICS, 
                new BigDecimal("149.99"));
        Product monitor = new Product("P005", "4K Monitor", ProductCategory.ELECTRONICS, 
                new BigDecimal("599.99"));
        
        // Create line items
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(new LineItem("LI001", laptop, 1, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI002", mouse, 2, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI003", coffee, 3, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI004", keyboard, 1, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI005", monitor, 1, BigDecimal.ZERO));
        
        // Create user
        User user = new User("LOGIN001", "USER001", new BigDecimal("5000.00"), 
                UserStatus.ACTIVE, "user@example.com");
        
        // Create shopping cart
        return new ShoppingCart(lineItems, user, BigDecimal.ZERO);
    }
    
    @Benchmark
    public boolean benchmarkSpEL() throws Exception {
        return spelEvaluator.evaluate(spelExpression, context);
    }
    
    @Benchmark
    public boolean benchmarkMVEL() throws Exception {
        return mvelEvaluator.evaluate(mvelExpression, context);
    }
    
    @Benchmark
    public boolean benchmarkJEXL() throws Exception {
        return jexlEvaluator.evaluate(jexlExpression, context);
    }
}

