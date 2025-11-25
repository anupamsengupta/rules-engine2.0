# Validation Engine Layer

This package contains the engine layer for evaluating rules and rule sets.

## Components

### Core Interfaces

- **`ValidationEngine`** - Main API for evaluating rule sets
- **`RuleExecutor`** - Executes individual rules (expression or Groovy script)
- **`ScriptLoader`** - Loads script content from various sources
- **`RuleSetProvider`** - Provides rule sets (with caching)

### Implementations

- **`DefaultValidationEngine`** - Main engine implementation
- **`ExpressionRuleExecutor`** - Executes expression-based rules
- **`GroovyScriptRuleExecutor`** - Executes Groovy script-based rules
- **`GroovyScriptCache`** - Tenant-aware cache for compiled Groovy scripts
- **`CompositeScriptLoader`** - Delegates to multiple script loaders
- **`FileSystemScriptLoader`** - Loads scripts from local file system
- **`S3ScriptLoader`** - Loads scripts from S3 (placeholder for AWS SDK integration)
- **`RuleSetResultCalculator`** - Calculates overall rule set status

### Factory

- **`ValidationEngineFactory`** - Factory for creating configured ValidationEngine instances

## Usage

### Basic Usage

```java
// Create a rule set provider (implementation from persistence layer)
RuleSetProvider provider = new JpaRuleSetProvider(...);

// Create validation engine
ValidationEngine engine = ValidationEngineFactory.createDefault(provider);

// Evaluate a rule set
Customer customer = new Customer("John", 25);
RuleSetResult result = engine.evaluate(
    "tenant-123",
    "customer-onboarding",
    "1.0",
    customer,
    Map.of("country", "DE", "channel", "WEB")
);

// Check result
if (result.isPassed()) {
    System.out.println("Validation passed!");
} else {
    System.out.println("Validation failed: " + result.overallStatus());
    result.ruleResults().stream()
        .filter(RuleResult::isFailure)
        .forEach(r -> System.out.println("  - " + r.ruleCode() + ": " + r.message()));
}
```

### Custom Configuration

```java
// Create custom script loader
CompositeScriptLoader scriptLoader = new CompositeScriptLoader();
scriptLoader.addLoader(new FileSystemScriptLoader());
scriptLoader.addLoader(new S3ScriptLoader());
// Add custom loaders...

// Create custom script cache
GroovyScriptCache scriptCache = new GroovyScriptCache();

// Create custom executors
List<RuleExecutor> executors = new ArrayList<>();
executors.add(new ExpressionRuleExecutor());
executors.add(new GroovyScriptRuleExecutor(scriptLoader, scriptCache));

// Create engine
ValidationEngine engine = ValidationEngineFactory.create(
    ruleSetProvider,
    executors,
    scriptLoader,
    scriptCache
);
```

## Groovy Script Execution

Groovy scripts receive the following variables:

- **`payload`** - The validation payload object
- **`context`** - Map of context attributes
- **`tenantId`** - The tenant identifier

Scripts should return a boolean:

```groovy
// Example Groovy script
return payload.age >= 18 && context.get("country") == "DE"
```

## Expression Evaluation

Expression rules use boolean expressions. Currently, expression evaluation is a placeholder.
For production use, integrate with Spring Expression Language (SpEL) or another expression library.

## Tenant-Aware Caching

- **Rule Sets**: Cached via `RuleSetProvider` (typically Redis-backed)
- **Groovy Scripts**: Cached in `GroovyScriptCache` (in-memory, per-tenant)

Script cache keys are tenant-aware: `{tenantId}:{scriptLocation}`

