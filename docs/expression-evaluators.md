# Expression Evaluator Configuration

The Validation Engine supports multiple expression evaluation engines that can be switched via configuration.

## Supported Evaluators

### 1. SpEL (Spring Expression Language) - Default

**Type:** `SPEL`

**Dependencies:** Included with Spring Framework

**Features:**
- Full Spring Expression Language support
- Type-safe property access
- Method invocation
- Collection operations

**Example Expression:**
```java
payload.age >= 18 && context['country'] == 'US'
```

### 2. MVEL (MVFLEX Expression Language)

**Type:** `MVEL`

**Dependencies:** `org.mvel:mvel2:2.4.14.Final`

**Features:**
- Fast execution
- Rich expression syntax
- Collection operations
- Type coercion

**Example Expression:**
```java
payload.age >= 18 && country == 'US'
```

### 3. JEXL (Java Expression Language)

**Type:** `JEXL`

**Dependencies:** `org.apache.commons:commons-jexl3:3.3`

**Features:**
- Apache Commons implementation
- Good performance
- Simple syntax
- Collection support

**Example Expression:**
```java
payload.age >= 18 && country == 'US'
```

## Configuration

### Application Properties

Configure the expression evaluator type in `application.yml`:

```yaml
quickysoft:
  validation:
    expression:
      evaluator-type: SPEL  # Options: SPEL, MVEL, JEXL
```

### Configuration Options

- **SPEL** (default): Spring Expression Language
- **MVEL**: MVFLEX Expression Language
- **JEXL**: Java Expression Language

## Expression Syntax Differences

### SpEL

```java
// Property access
payload.age >= 18

// Context variable access
context['country'] == 'US'

// Method calls
payload.email.contains('@')

// Collection operations
context['allowedCountries'].contains(payload.country)
```

### MVEL

```java
// Property access
payload.age >= 18

// Context variable (direct access)
country == 'US'

// Method calls
payload.email.contains('@')

// Collection operations
allowedCountries.contains(payload.country)
```

### JEXL

```java
// Property access
payload.age >= 18

// Context variable (direct access)
country == 'US'

// Method calls
payload.email.contains('@')

// Collection operations
allowedCountries.contains(payload.country)
```

## Variable Access

All evaluators provide access to:

- **`payload`**: The validation payload object
- **`context`**: Map of context attributes (SpEL only - use `context['key']`)
- **Context attributes as direct variables**: In MVEL and JEXL, context attributes are available as direct variables

## Performance Considerations

- **SpEL**: Good performance, excellent Spring integration
- **MVEL**: Very fast, good for high-throughput scenarios
- **JEXL**: Good performance, Apache Commons ecosystem

## Switching Evaluators

To switch evaluators:

1. Update `application.yml`:
```yaml
quickysoft:
  validation:
    expression:
      evaluator-type: MVEL  # or JEXL
```

2. Ensure the required dependency is on the classpath:
   - MVEL: `org.mvel:mvel2`
   - JEXL: `org.apache.commons:commons-jexl3`

3. Restart the application

The auto-configuration will automatically create the correct evaluator based on the configuration.

## Custom Evaluator

To use a custom evaluator:

1. Implement `ExpressionEvaluator` interface
2. Create a `@Bean` of type `ExpressionEvaluator`
3. The auto-configuration will use your custom bean instead of creating a default one

## Examples

### Example 1: Age Validation (All Evaluators)

```java
// SpEL, MVEL, JEXL
payload.age >= 18
```

### Example 2: Country and Channel Check

```java
// SpEL
payload.country == 'US' && context['channel'] == 'WEB'

// MVEL / JEXL
payload.country == 'US' && channel == 'WEB'
```

### Example 3: Complex Validation

```java
// SpEL
payload.age >= 18 && 
payload.email.contains('@') && 
context['allowedCountries'].contains(payload.country)

// MVEL / JEXL
payload.age >= 18 && 
payload.email.contains('@') && 
allowedCountries.contains(payload.country)
```

