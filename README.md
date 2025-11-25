# Validation Engine 2.0

A modular, multi-tenant Java 17 rule-based validation engine designed as a reusable library for Spring Boot applications.

## Overview

The Validation Engine provides a flexible, scalable solution for implementing business rules and validation logic across multiple tenants. It supports both expression-based rules and Groovy script-based rules, with centralized caching via Redis/ElastiCache.

## Architecture

### Modules

1. **`validation-engine-core`**
   - Pure Java library with no HTTP dependencies
   - Canonical domain model (RuleSet, Rule, ValidationContext)
   - Evaluation framework (RuleEvaluationResult, RuleSetEvaluationResult)
   - Engine interfaces (ValidationEngine, RuleExecutor, ScriptLoader)
   - Provider abstraction (RuleSetProvider)

2. **`validation-engine-persistence`**
   - JPA entities and repositories (Spring Data JPA)
   - Redis caching layer with write-through pattern
   - Provider implementation (JpaRuleSetProvider) with Redis-first strategy
   - Mappers between entities and domain models
   - **Auto-configuration** for Spring Boot applications

3. **`validation-engine-admin-api`**
   - Spring Boot REST API for CRUD operations
   - DTOs and request/response models
   - Controllers, services, and mappers
   - Write-through caching on create/update/delete

4. **`validation-engine-sample-app`**
   - Sample Spring Boot application demonstrating usage
   - Example domain classes and services
   - REST endpoints for validation

## Multi-Tenancy Model

All operations in the validation engine are **tenant-scoped**:

- **Tenant Identification**: Every rule set and rule belongs to a `tenantId`
- **Uniqueness Constraints**: Per-tenant (e.g., `(tenantId, code, version)`)
- **Isolation**: Tenant data is isolated at database and cache levels
- **URL Pattern**: Admin API uses `/tenants/{tenantId}/...` for tenant scoping

### How TenantId Flows

```
Application Code
    ↓
ValidationEngine.evaluate(tenantId, ruleSetCode, version, payload, context)
    ↓
RuleSetProvider.getRuleSet(tenantId, ruleSetCode, version)
    ↓
RuleSetCache.getRuleSet(tenantId, ruleSetCode, version)  [Redis-first]
    ↓
JPA Repository (fallback if not in cache)
```

## Getting Started

### 1. Add Dependencies

In your Spring Boot application's `pom.xml`:

```xml
<dependencies>
    <!-- Core validation engine -->
    <dependency>
        <groupId>com.validationengine</groupId>
        <artifactId>validation-engine-core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    
    <!-- Persistence layer with auto-configuration -->
    <dependency>
        <groupId>com.validationengine</groupId>
        <artifactId>validation-engine-persistence</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### 2. Configure Database and Redis

In your `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/validationdb
    username: your-username
    password: your-password
  
  jpa:
    hibernate:
      ddl-auto: update
  
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

# Validation Engine Configuration
quickysoft:
  validation:
    cache:
      enabled: true
      ttl-hours: 24
    script:
      local-base-path: /scripts
      s3:
        enabled: false
        # bucket: my-scripts-bucket
        # region: us-east-1
```

### 3. Auto-Configuration

The validation engine is **automatically configured** when `validation-engine-persistence` is on the classpath. No additional configuration needed!

The following beans are auto-configured:

- `ValidationEngine` - Main API for rule evaluation
- `RuleSetProvider` - JPA-based provider with Redis caching
- `RuleSetCache` - Redis cache (or NoOp if Redis unavailable)
- `ScriptLoader` - Composite loader (file system + S3)
- `GroovyScriptCache` - In-memory cache for compiled Groovy scripts
- Rule executors (ExpressionRuleExecutor, GroovyScriptRuleExecutor)

### 4. Use the Validation Engine

Inject `ValidationEngine` in your service:

```java
@Service
public class CustomerService {
    
    private final ValidationEngine validationEngine;
    
    public CustomerService(ValidationEngine validationEngine) {
        this.validationEngine = validationEngine;
    }
    
    public RuleSetResult validateCustomer(String tenantId, Customer customer) {
        return validationEngine.evaluate(
            tenantId,
            "customer-onboarding",
            "1.0",
            customer,
            Map.of("channel", "WEB", "country", "US")
        );
    }
}
```

## Caching Architecture

### Write-Through Caching Pattern

The validation engine uses a **write-through caching** pattern:

1. **Admin API (Write Operations)**:
   - Creates/updates rule sets in database
   - **Immediately updates Redis cache** (write-through)
   - On delete, **evicts from Redis cache**

2. **Microservices (Read Operations)**:
   - **First checks Redis cache** (fast)
   - Falls back to JPA if not in cache
   - Caches result for future use

### Cache Key Pattern

Cache keys follow the pattern: `ruleset:{tenantId}:{ruleSetCode}:{version}`

Example: `ruleset:tenant-123:customer-onboarding:1.0`

### Benefits

- **Centralized Cache**: All services share the same Redis/ElastiCache cluster
- **No Local Caching Needed**: Services don't need per-instance caches
- **Consistency**: Admin API updates ensure all services see changes
- **Performance**: Fast reads from Redis, fallback to DB if needed

## Rule Types

### Expression Rules

Expression-based rules evaluate boolean expressions (e.g., SpEL):

```java
ExpressionRule rule = ExpressionRule.builder()
    .tenantId("tenant-123")
    .ruleCode("age-check")
    .name("Age Validation")
    .expression("payload.age >= 18")
    .severity(Severity.ERROR)
    .build();
```

### Groovy Script Rules

Groovy script-based rules execute external scripts:

```java
GroovyScriptRule rule = GroovyScriptRule.builder()
    .tenantId("tenant-123")
    .ruleCode("country-check")
    .name("Country Validation")
    .scriptLocationType(ScriptLocationType.INLINE)
    .scriptReference("return context.get('country') in ['DE', 'US', 'UK']")
    .severity(Severity.WARN)
    .build();
```

Script locations supported:
- `LOCAL_FILE` - Local file system path
- `S3_OBJECT` - S3 bucket and key
- `INLINE` - Script content stored directly

## Script Loading

### Local File System

Scripts can be loaded from the local file system:

```yaml
quickysoft:
  validation:
    script:
      local-base-path: /var/validation-scripts
```

Scripts are referenced by path relative to the base path.

### S3 (AWS)

Scripts can be loaded from S3:

```yaml
quickysoft:
  validation:
    script:
      s3:
        enabled: true
        bucket: my-scripts-bucket
        region: us-east-1
        key-prefix: scripts/
```

Scripts are referenced by S3 key (with optional prefix).

## Admin API

The admin API provides REST endpoints for managing rules:

### Rule Sets

- `POST /tenants/{tenantId}/rulesets` - Create rule set
- `GET /tenants/{tenantId}/rulesets` - List rule sets
- `GET /tenants/{tenantId}/rulesets/{code}/versions/{version}` - Get rule set
- `PUT /tenants/{tenantId}/rulesets/{code}/versions/{version}` - Update rule set
- `DELETE /tenants/{tenantId}/rulesets/{code}/versions/{version}` - Delete rule set

### Rules

- `POST /tenants/{tenantId}/rulesets/{ruleSetCode}/versions/{version}/rules` - Add rule
- `PUT /tenants/{tenantId}/rulesets/{ruleSetCode}/versions/{version}/rules/{ruleCode}` - Update rule
- `DELETE /tenants/{tenantId}/rulesets/{ruleSetCode}/versions/{version}/rules/{ruleCode}` - Delete rule

### Swagger UI

Access API documentation at: `http://localhost:8080/swagger-ui.html`

## Example: Customer Validation

See `validation-engine-sample-app` for a complete example:

```java
@RestController
@RequestMapping("/tenants/{tenantId}/validate-customer")
public class CustomerValidationController {
    
    private final ValidationEngine validationEngine;
    
    @PostMapping
    public ResponseEntity<RuleSetResult> validateCustomer(
            @PathVariable String tenantId,
            @RequestBody Customer customer
    ) {
        RuleSetResult result = validationEngine.evaluate(
            tenantId,
            "customer-onboarding",
            "1.0",
            customer,
            Map.of("channel", "WEB")
        );
        
        return ResponseEntity.ok(result);
    }
}
```

## Configuration Properties

### Cache Configuration

```yaml
quickysoft:
  validation:
    cache:
      enabled: true          # Enable/disable caching
      ttl-hours: 24          # Cache TTL in hours
```

### Script Configuration

```yaml
quickysoft:
  validation:
    script:
      local-base-path: /scripts
      s3:
        enabled: false
        bucket: my-bucket
        region: us-east-1
        key-prefix: scripts/
```

## Building

```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl validation-engine-core

# Run sample app
cd validation-engine-sample-app
mvn spring-boot:run
```

## Running the Admin API

```bash
cd validation-engine-admin-api
mvn spring-boot:run
```

Access at: `http://localhost:8080`

## Running the Sample App

```bash
cd validation-engine-sample-app
mvn spring-boot:run
```

Access at: `http://localhost:8081`

## Technology Stack

- **Java 17** (records, pattern matching, sealed interfaces)
- **Spring Boot 3.2.0**
- **Spring Data JPA** (Hibernate)
- **Spring Data Redis** (Lettuce)
- **Groovy 4.0.15** (for script-based rules)
- **Maven** (multi-module build)

## Design Principles

- **Separation of Concerns**: Clear boundaries between core, persistence, and API layers
- **Multi-Tenancy First**: Tenant isolation at every level
- **Caching Strategy**: Redis-first with JPA fallback, write-through on updates
- **Extensibility**: Interfaces for rule execution, script loading, and providers
- **Java 17 Features**: Leverages records, pattern matching, and modern Java idioms
- **Auto-Configuration**: Works out of the box with minimal configuration

## License

Apache 2.0

## Support

For issues and questions, please contact the Validation Engine Team.
