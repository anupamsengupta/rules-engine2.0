package com.quickysoft.validation.core.engine.benchmark;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;
import com.quickysoft.validation.core.engine.expression.ExpressionEvaluatorType;
import com.quickysoft.validation.core.engine.expression.impl.ExpressionEvaluatorFactory;
import com.quickysoft.validation.core.model.ValidationContext;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark for simple expression evaluation across SpEL, MVEL, and JEXL.
 * 
 * Simple case: Basic context variable comparison
 * Expression: context.channel == 'WEB'
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
public class SimpleExpressionBenchmark {
    
    private ExpressionEvaluator spelEvaluator;
    private ExpressionEvaluator mvelEvaluator;
    private ExpressionEvaluator jexlEvaluator;
    
    private ValidationContext<Object> context;
    
    // SpEL expression
    private String spelExpression = "#context.channel == 'WEB'";
    
    // MVEL expression
    private String mvelExpression = "context.channel == 'WEB'";
    
    // JEXL expression
    private String jexlExpression = "context.channel == 'WEB'";
    
    @Setup
    public void setup() {
        ExpressionEvaluatorFactory factory = ExpressionEvaluatorFactory.getInstance();
        spelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.SPEL);
        mvelEvaluator = factory.getEvaluator(ExpressionEvaluatorType.MVEL);
        jexlEvaluator = factory.getEvaluator(ExpressionEvaluatorType.JEXL);
        
        // Create validation context with test data
        Map<String, Object> contextAttributes = Map.of("channel", "WEB", "country", "US");
        context = new ValidationContext<>("test-payload", contextAttributes);
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

