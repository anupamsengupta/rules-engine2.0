# Validation Engine Persistence Layer

Multi-tenant persistence layer for rules and rule sets using Spring Data JPA and Redis/ElastiCache.

## Features

- **Multi-tenant JPA entities** with tenant isolation
- **Spring Data repositories** with tenant-aware queries
- **Redis/ElastiCache caching** with write-through semantics
- **Automatic configuration** via Spring Boot auto-configuration

## Components

### Entities

- `RuleSetEntity` - JPA entity for rule sets
- `RuleEntity` - JPA entity for rules (supports both expression and Groovy script types)
- `MetadataConverter` - JPA converter for Map<String, String> to JSON

### Repositories

- `RuleSetRepository` - Spring Data repository for rule sets
- `RuleRepository` - Spring Data repository for rules

### Mappers

- `RuleSetMapper` - Maps between JPA entities and canonical domain models

### Providers

- `JpaRuleSetProvider` - Implements `RuleSetProvider` with Redis-first caching strategy

### Cache

- `RuleSetCache` - Interface for rule set caching
- `RedisRuleSetCache` - Redis/ElastiCache implementation

### Configuration

- `ValidationPersistenceConfig` - Auto-configuration for persistence layer

## Usage

### Auto-Configuration

The persistence layer is automatically configured when the module is on the classpath:

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### Manual Configuration

If you need custom configuration:

```java
@Configuration
public class CustomPersistenceConfig {
    
    @Bean
    public RuleSetCache customCache(RedisTemplate<String, String> redisTemplate) {
        return new RedisRuleSetCache(redisTemplate, new ObjectMapper());
    }
}
```

## Database Schema

### Rule Sets Table

```sql
CREATE TABLE rule_sets (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    version VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (tenant_id, code, version)
);
```

### Rules Table

```sql
CREATE TABLE rules (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    rule_set_id UUID NOT NULL REFERENCES rule_sets(id),
    rule_code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    priority INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT true,
    severity VARCHAR(20) NOT NULL,
    rule_type VARCHAR(20) NOT NULL,
    expression TEXT,
    script_location_type VARCHAR(20),
    script_reference VARCHAR(500),
    script_version VARCHAR(50),
    script_checksum VARCHAR(64),
    failure_message_template TEXT,
    metadata TEXT,
    applicable_contexts VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE (tenant_id, rule_set_id, rule_code)
);
```

## Redis Cache Keys

Cache keys follow the pattern: `ruleset:{tenantId}:{ruleSetCode}:{version}`

Example: `ruleset:tenant-123:customer-onboarding:1.0`

## Configuration Properties

See `application.yml.example` for configuration properties:

- Database connection settings
- JPA/Hibernate settings
- Redis connection settings

## Testing

The module includes integration tests that verify:

- Saving and loading rule sets via JPA
- Mapping between entities and domain models
- Redis caching with write-through semantics
- Multi-tenant isolation

Run tests with:

```bash
mvn test
```

