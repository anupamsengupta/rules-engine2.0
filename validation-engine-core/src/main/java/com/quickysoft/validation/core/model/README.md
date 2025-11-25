# Canonical Multi-Tenant Domain Model

This package contains the canonical domain model for the rule-based validation engine.

## Package Structure

```
com.quickysoft.validation.core.model/
├── Severity.java              # Enum: INFO, WARN, ERROR
├── RuleStatus.java            # Enum: PASSED, FAILED, SKIPPED, ERROR
├── RuleSetStatus.java         # Enum: PASS, WARN, FAIL, ERROR
├── ScriptLocationType.java    # Enum: LOCAL_FILE, S3_OBJECT, INLINE
├── Rule.java                  # Sealed interface for rules
├── ExpressionRule.java        # Expression-based rule implementation
├── GroovyScriptRule.java      # Groovy script-based rule implementation
├── RuleSet.java               # Collection of rules
├── ValidationContext.java     # Generic validation context with payload
├── RuleResult.java            # Result of evaluating a single rule
├── RuleSetResult.java         # Result of evaluating a rule set
├── package-info.java          # Package documentation
└── README.md                  # This file
```

## Key Features

### Java 17 Features Used
- **Records**: All domain types are records for immutability
- **Sealed Interfaces**: `Rule` is a sealed interface with two permitted implementations
- **Pattern Matching Ready**: Sealed interface enables pattern matching in switch expressions

### Multi-Tenancy
- All types explicitly include `tenantId` for tenant isolation
- Uniqueness constraints are per-tenant
- Tenant context flows through evaluation and results

### Builders
- `RuleSet`, `ExpressionRule`, and `GroovyScriptRule` provide builder patterns
- `RuleSetResult` provides a builder for constructing results
- Factory methods in `RuleResult` for common scenarios

### Type Safety
- `ValidationContext<T>` is generic for type-safe payload access
- Sealed interface prevents unauthorized rule implementations
- Strong typing throughout

## Usage Examples

See `package-info.java` for detailed usage examples and documentation.

