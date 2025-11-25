# Groovy Script Contract for Validation Engine

This document describes the contract for writing Groovy validation scripts used in the Validation Engine.

## Overview

Groovy scripts are used to implement complex validation logic that cannot be expressed as simple boolean expressions. Scripts are executed in a controlled environment with access to the validation payload, context attributes, and execution metadata.

## Script Contract

### Input Variables

When a Groovy script executes, the following variables are available:

#### 1. `payload` (Object)
The primary object being validated. This is the same object passed to `ValidationEngine.evaluate()`.

**Example:**
```groovy
// If validating a Customer object
def age = payload.age
def email = payload.email
```

#### 2. `context` (Map<String, Object>)
Additional context attributes provided during validation. Common attributes include:
- `channel`: The channel through which the request came (e.g., "WEB", "MOBILE")
- `country`: The country code
- `userId`: The user performing the action
- `market`: The market identifier

**Example:**
```groovy
def channel = context.get('channel')
def country = context.get('country')
```

#### 3. `executionContext` (RuleExecutionContext)
A helper object containing metadata about the rule execution:
- `ruleId`: UUID of the rule
- `tenantId`: The tenant identifier
- `ruleSetCode`: The rule set code
- `ruleCode`: The rule code
- `ruleName`: The rule name
- `metadata`: Map of rule metadata

**Example:**
```groovy
def tenantId = executionContext.tenantId()
def ruleCode = executionContext.ruleCode()
def customValue = executionContext.getMetadata('customKey')
```

#### 4. `tenantId` (String)
Convenience variable for the tenant identifier. Same as `executionContext.tenantId()`.

**Example:**
```groovy
// Tenant-specific logic
if (tenantId == 'tenant-123') {
    // Special handling for this tenant
}
```

### Return Values

Scripts must return one of the following:

#### Option 1: Boolean
Return `true` for pass, `false` for fail.

**Example:**
```groovy
// Simple validation
return payload.age >= 18 && payload.email.contains('@')
```

#### Option 2: Map with Status and Message
Return a map with the following keys:
- `status` (Boolean): `true` for pass, `false` for fail
- `message` (String, optional): Custom failure message
- `details` (Map<String, Object>, optional): Additional details for the failure

**Example:**
```groovy
if (payload.age < 18) {
    return [
        status: false,
        message: "Customer must be at least 18 years old",
        details: [
            currentAge: payload.age,
            requiredAge: 18
        ]
    ]
}
return [status: true]
```

## Multi-Tenancy

### Tenant-Aware Scripts

Scripts can access the `tenantId` through:
- `executionContext.tenantId()`
- `tenantId` variable (convenience)

This allows scripts to implement tenant-specific validation logic:

```groovy
// Different age requirements per tenant
def minAge = 18
if (tenantId == 'tenant-123') {
    minAge = 21  // Higher age requirement for this tenant
}

return payload.age >= minAge
```

### Tenant-Scoped Script Storage

When using S3 script storage with tenant-scoped paths enabled, scripts are stored at:
```
s3://bucket/scripts/{tenantId}/{scriptReference}
```

This allows each tenant to have their own script versions.

## Script Examples

### Example 1: Simple Boolean Return

```groovy
// Validate that customer age is at least 18
return payload.age >= 18
```

### Example 2: Boolean with Context Check

```groovy
// Validate country is allowed based on channel
def allowedCountries = ['US', 'CA', 'UK']
if (context.get('channel') == 'MOBILE') {
    allowedCountries = ['US', 'CA', 'UK', 'DE', 'FR']
}
return allowedCountries.contains(payload.country)
```

### Example 3: Map Return with Custom Message

```groovy
// Validate email format and domain
def email = payload.email
if (!email || !email.contains('@')) {
    return [
        status: false,
        message: "Invalid email format",
        details: [email: email]
    ]
}

def domain = email.split('@')[1]
def allowedDomains = ['example.com', 'test.com']
if (!allowedDomains.contains(domain)) {
    return [
        status: false,
        message: "Email domain not allowed",
        details: [
            email: email,
            domain: domain,
            allowedDomains: allowedDomains
        ]
    ]
}

return [status: true]
```

### Example 4: Tenant-Specific Logic

```groovy
// Different validation rules per tenant
def minAge = 18
def allowedCountries = ['US', 'CA']

if (executionContext.tenantId() == 'tenant-premium') {
    minAge = 21
    allowedCountries = ['US', 'CA', 'UK', 'DE', 'FR', 'AU']
}

def ageValid = payload.age >= minAge
def countryValid = allowedCountries.contains(payload.country)

if (!ageValid) {
    return [
        status: false,
        message: "Age requirement not met for tenant ${executionContext.tenantId()}",
        details: [
            currentAge: payload.age,
            requiredAge: minAge,
            tenantId: executionContext.tenantId()
        ]
    ]
}

if (!countryValid) {
    return [
        status: false,
        message: "Country not allowed for tenant ${executionContext.tenantId()}",
        details: [
            country: payload.country,
            allowedCountries: allowedCountries,
            tenantId: executionContext.tenantId()
        ]
    ]
}

return [status: true]
```

### Example 5: Complex Business Logic

```groovy
// Validate customer onboarding with multiple checks
def errors = []

// Age check
if (payload.age < 18) {
    errors.add("Customer must be at least 18 years old")
}

// Email validation
if (!payload.email || !payload.email.matches(/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/)) {
    errors.add("Invalid email format")
}

// Country and channel combination
def country = payload.country
def channel = context.get('channel')
if (country == 'DE' && channel == 'MOBILE') {
    errors.add("Mobile channel not available for Germany")
}

if (errors.isEmpty()) {
    return [status: true]
} else {
    return [
        status: false,
        message: "Validation failed: ${errors.join(', ')}",
        details: [
            errors: errors,
            tenantId: executionContext.tenantId(),
            ruleCode: executionContext.ruleCode()
        ]
    ]
}
```

## Error Handling

### Script Execution Errors

If a script throws an exception during execution:
- The rule evaluation will return `RuleResult` with status `ERROR`
- The exception message will be included in the result
- The `tenantId` and `ruleCode` will be included for diagnostics

### Script Loading Errors

If a script cannot be loaded (e.g., S3 404, file not found):
- A `ScriptLoadException` is thrown
- The rule evaluation will return `RuleResult` with status `ERROR`
- Error details include `tenantId` and script location for diagnostics

## Best Practices

1. **Always return a value**: Scripts must return either a boolean or a map
2. **Use executionContext for tenant-aware logic**: Access `tenantId` through `executionContext.tenantId()`
3. **Provide meaningful error messages**: When returning a map, include a clear `message`
4. **Include diagnostic details**: Use the `details` map to provide context for failures
5. **Handle null values**: Check for null before accessing object properties
6. **Keep scripts focused**: Each script should validate one aspect or a cohesive set of related checks
7. **Logging**: Use Groovy's `println` for debugging (logged at DEBUG level)

## Script Location Types

Scripts can be stored in three ways:

1. **INLINE**: Script content stored directly in the database
   - Use for small, frequently-used scripts
   - No external dependencies

2. **LOCAL_FILE**: Script stored on local file system
   - Path: `/scripts/{scriptReference}`
   - Use for scripts that need version control

3. **S3_OBJECT**: Script stored in S3 bucket
   - Key: `s3://bucket/scripts/{tenantId}/{scriptReference}` (if tenant-scoped)
   - Use for distributed deployments

## Versioning and Caching

- Scripts are cached in memory after first load
- Script version/checksum is stored in the database for validation
- Cache is tenant-aware: `{tenantId}:{scriptReference}`
- Cache can be evicted per tenant or per script

## Testing Scripts

When testing scripts locally:

1. Create a test script file
2. Use the inline script location type
3. Test with sample payload and context
4. Verify return values match expected format

Example test payload:
```groovy
def payload = [
    age: 25,
    email: "test@example.com",
    country: "US"
]
def context = [
    channel: "WEB",
    userId: "user-123"
]
```

