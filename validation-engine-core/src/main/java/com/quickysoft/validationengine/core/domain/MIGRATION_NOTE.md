# Migration Note

The domain model has been moved to the new canonical package structure.

## Old Package (Deprecated)
- `com.validationengine.core.domain.*`

## New Package (Canonical)
- `com.quickysoft.validation.core.model.*`

## Migration Path

The following types have been redesigned:

1. **RuleSet** → `com.quickysoft.validation.core.model.RuleSet`
   - Now uses UUID for id
   - Uses `code` instead of `ruleSetCode`
   - Added `active` flag
   - Rules are now sealed interface implementations

2. **Rule** → `com.quickysoft.validation.core.model.Rule` (sealed interface)
   - `ExpressionRule` - for expression-based rules
   - `GroovyScriptRule` - for Groovy script-based rules
   - Added severity, applicableContexts, metadata

3. **ValidationContext** → `com.quickysoft.validation.core.model.ValidationContext<T>`
   - Now generic with payload type
   - Renamed `attributes` to `contextAttributes`

4. **RuleEvaluationResult** → `com.quickysoft.validation.core.model.RuleResult`
   - Added UUID ruleId
   - Added severity
   - Changed status enum (PASSED/FAILED/SKIPPED/ERROR)

5. **RuleSetEvaluationResult** → `com.quickysoft.validation.core.model.RuleSetResult`
   - Added UUID ruleSetId
   - Changed status enum (PASS/WARN/FAIL/ERROR)

## Next Steps

1. Update engine interfaces to use new model
2. Update persistence layer mappers
3. Update admin API DTOs
4. Remove old domain classes once migration is complete

