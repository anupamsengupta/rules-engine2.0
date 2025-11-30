# Validation Engine Benchmarks

This module contains JMH (Java Microbenchmark Harness) benchmarks to compare the performance of SpEL, MVEL, and JEXL expression evaluators.

## Benchmark Classes

### 1. SimpleExpressionBenchmark
**Simple case**: Basic context variable comparison
- Expression: `context.channel == 'WEB'`
- Tests basic variable access and comparison

### 2. MediumComplexExpressionBenchmark
**Medium complex case**: Multiple conditions with payload and context variables
- Expression: `payload.age >= 18 && context.channel == 'WEB' && context.country == 'US'`
- Tests multiple conditions, payload access, and context variable access

### 3. LineItemVerificationBenchmark
**Line item verification case**: Calculate sum of line items and compare with cart total
- Expression: Sum of `(quantity * product.price)` == `cartTotalAmount`
- Tests collection iteration, BigDecimal operations, and complex calculations

## Running the Benchmarks

### Using Maven

```bash
# Compile the benchmark module
mvn clean compile -pl validation-benchmark

# Run all benchmarks
mvn exec:java -Dexec.args=".*Benchmark" -pl validation-benchmark

# Run specific benchmark
mvn exec:java -Dexec.args="SimpleExpressionBenchmark" -pl validation-benchmark

# Run with custom JMH options
mvn exec:java -Dexec.args="-f 3 -wi 5 -i 10 SimpleExpressionBenchmark" -pl validation-benchmark
```

**Note**: The `exec-maven-plugin` is configured in the `pom.xml` with `mainClass` set to `org.openjdk.jmh.Main`, so you only need to pass arguments via `-Dexec.args`.

### Using Helper Scripts

**Linux/Mac:**
```bash
cd validation-benchmark
./src/main/resources/RunBenchmarks.sh
```

**Windows:**
```cmd
cd validation-benchmark
src\main\resources\RunBenchmarks.bat
```

## Benchmark Configuration

All benchmarks use the following configuration:
- **Mode**: AverageTime (nanoseconds per operation)
- **Warmup**: 5 iterations, 1 second each
- **Measurement**: 10 iterations, 1 second each
- **Forks**: 2 (to reduce JVM variance)

## Interpreting Results

The benchmarks will output results showing:
- **Score**: Average time per operation in nanoseconds (lower is better)
- **Error**: Statistical error margin
- **Units**: nanoseconds per operation

Example output:
```
Benchmark                                    Mode  Cnt      Score      Error  Units
SimpleExpressionBenchmark.benchmarkJEXL     avgt   20   1234.567 ±   45.678  ns/op
SimpleExpressionBenchmark.benchmarkMVEL     avgt   20    987.654 ±   32.123  ns/op
SimpleExpressionBenchmark.benchmarkSpEL     avgt   20   1567.890 ±   67.890  ns/op
```

## Notes

- Benchmarks are run in separate JVM forks to ensure clean state
- Results may vary based on JVM version, OS, and hardware
- For production decisions, run benchmarks on target hardware
- Consider both performance and feature set when choosing an evaluator

