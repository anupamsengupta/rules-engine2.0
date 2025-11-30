package com.quickysoft.validation.core.engine.benchmark;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.model.ValidationContext;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for medium complexity expression evaluation across SpEL, MVEL, and JEXL.
 * 
 * Medium complex case: Multiple conditions with payload and context variables
 * Expression: payload.age >= 18 && context.channel == 'WEB' && context.country == 'US'
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class MediumComplexExpressionBenchmark {
    
    private record Customer(String name, Integer age, String email) {
    }
    
    private ExpressionEvaluator spelEvaluator;
    private ExpressionEvaluator mvelEvaluator;
    private ExpressionEvaluator jexlEvaluator;
    
    private ValidationContext<Customer> context;
    
    // SpEL expression
    private String spelExpression = "#payload.age >= 18 && #context.channel == 'WEB' && #context.country == 'US'";
    
    // MVEL expression
    private String mvelExpression = "payload.age >= 18 && context.channel == 'WEB' && context.country == 'US'";
    
    // JEXL expression
    private String jexlExpression = "payload.age >= 18 && context.channel == 'WEB' && context.country == 'US'";
    
    @Setup
    public void setup() {
        ExpressionEvaluatorFactory factory = ExpressionEvaluatorFactory.getInstance();
        spelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.SPEL);
        mvelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.MVEL);
        jexlEvaluator = factory.getEvaluator(ExpressionEvaluatorType.JEXL);
        
        // Create customer payload
        Customer customer = new Customer("John", 25, "john@example.com");
        
        // Create validation context with test data
        Map<String, Object> contextAttributes = Map.of("channel", "WEB", "country", "US");
        context = new ValidationContext<>(customer, contextAttributes);
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

