# Expression Evaluator Strategy

The Validation Engine supports multiple expression evaluation engines that can be switched via Spring Boot configuration.

## Strategy Pattern Implementation

The `ExpressionRuleExecutor` uses the **Strategy Pattern** with the `ExpressionEvaluator` interface to support multiple expression engines:

- **SpEL** (Spring Expression Language) - Default
- **MVEL** (MVFLEX Expression Language)
- **JEXL** (Java Expression Language)

## Configuration

Configure the evaluator type in `application.yml`:

```yaml
quickysoft:
  validation:
    expression:
      evaluator-type: SPEL  # Options: SPEL, MVEL, JEXL
```

## How It Works

1. **Individual Evaluator Beans**: Each evaluator (SpEL, MVEL, JEXL) is created as a separate bean conditionally based on class availability
2. **Primary Evaluator Bean**: A `@Primary` bean selects the appropriate evaluator from available evaluators based on configuration
3. **ExpressionRuleExecutor**: Injects the primary `ExpressionEvaluator` bean and uses it for evaluation

## Switching Evaluators

To switch from SpEL to MVEL:

```yaml
quickysoft:
  validation:
    expression:
      evaluator-type: MVEL
```

To switch to JEXL:

```yaml
quickysoft:
  validation:
    expression:
      evaluator-type: JEXL
```

## Custom Evaluator

To provide a custom evaluator:

1. Implement `ExpressionEvaluator` interface
2. Create a `@Bean` of type `ExpressionEvaluator`
3. The auto-configuration will use your custom bean

## Expression Syntax

Each evaluator has slightly different syntax:

- **SpEL**: `context['key']` for context variables
- **MVEL**: Direct variable access (context variables are available as direct variables)
- **JEXL**: Direct variable access (context variables are available as direct variables)

See `docs/expression-evaluators.md` for detailed syntax differences and examples.
