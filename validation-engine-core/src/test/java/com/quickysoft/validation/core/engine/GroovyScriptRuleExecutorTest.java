package com.quickysoft.validation.core.engine;

import com.quickysoft.validation.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GroovyScriptRuleExecutor.
 */
class GroovyScriptRuleExecutorTest {
    
    private GroovyScriptRuleExecutor executor;
    private ScriptLoader scriptLoader;
    private GroovyScriptCache scriptCache;
    
    @BeforeEach
    void setUp() {
        scriptLoader = new ScriptLoader() {
            @Override
            public String loadScript(String tenantId, String location) throws ScriptLoadException {
                // Return inline scripts based on location
                if ("inline-script-boolean".equals(location)) {
                    return "return payload.age() >= 18";
                }
                if ("inline-script-map".equals(location)) {
                    return """
                        if (payload.age() < 18) {
                            return [
                                status: false,
                                message: "Customer must be at least 18 years old",
                                details: [
                                    currentAge: payload.age(),
                                    requiredAge: 18,
                                    tenantId: executionContext.tenantId()
                                ]
                            ]
                        }
                        return [status: true]
                        """;
                }
                if ("inline-script-tenant-aware".equals(location)) {
                    return """
                        def minAge = 18
                        if (executionContext.tenantId() == 'tenant-premium') {
                            minAge = 21
                        }
                        return payload.age() >= minAge
                        """;
                }
                if ("s3-script-404".equals(location)) {
                    throw new ScriptLoadException("Script not found in S3: " + location);
                }
                throw new ScriptLoadException("Unknown script: " + location);
            }
            
            @Override
            public boolean supports(String location) {
                return true;
            }
        };
        
        scriptCache = new GroovyScriptCache();
        executor = new GroovyScriptRuleExecutor(scriptLoader, scriptCache);
    }
    
    @Test
    void testBooleanReturnScript() throws RuleExecutionException {
        // Create rule
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("age-check")
                .name("Age Validation")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.LOCAL_FILE)
                .scriptReference("inline-script-boolean")
                .build();
        
        // Create validation context
        Customer customer = new Customer("customer-1", 25);
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Execute
        RuleResult result = executor.execute(rule, context, "test-ruleset");
        
        // Debug: print error if status is ERROR
        if (result.status() == RuleStatus.ERROR && result.error() != null) {
            System.err.println("Script execution error: " + result.error().getMessage());
            result.error().printStackTrace();
        }
        
        // Verify
        assertThat(result.status()).isEqualTo(RuleStatus.PASSED);
        assertThat(result.ruleCode()).isEqualTo("age-check");
        assertThat(result.tenantId()).isEqualTo("tenant-123");
    }
    
    @Test
    void testBooleanReturnScriptFailure() throws RuleExecutionException {
        // Create rule
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("age-check")
                .name("Age Validation")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.INLINE)
                .scriptReference("inline-script-boolean")
                .build();
        
        // Create validation context with underage customer
        Customer customer = new Customer("customer-1", 16);
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Execute
        RuleResult result = executor.execute(rule, context, "test-ruleset");
        
        // Verify
        assertThat(result.status()).isEqualTo(RuleStatus.ERROR);
        assertThat(result.ruleCode()).isEqualTo("age-check");
    }
    
    @Test
    void testMapReturnScript() throws RuleExecutionException {
        // Create rule
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("age-check-detailed")
                .name("Age Validation with Details")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.LOCAL_FILE)
                .scriptReference("inline-script-map")
                .build();
        
        // Create validation context with underage customer
        Customer customer = new Customer("customer-1", 16);
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Execute
        RuleResult result = executor.execute(rule, context, "test-ruleset");
        
        // Verify
        assertThat(result.status()).isEqualTo(RuleStatus.FAILED);
        assertThat(result.message()).contains("at least 18 years old");
        assertThat(result.details()).containsKey("currentAge");
        assertThat(result.details()).containsKey("requiredAge");
        assertThat(result.details()).containsKey("tenantId");
        assertThat(result.details().get("tenantId")).isEqualTo("tenant-123");
    }
    
    @Test
    void testTenantAwareScript() throws RuleExecutionException {
        // Create rule for premium tenant
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-premium")
                .ruleCode("age-check-premium")
                .name("Premium Age Validation")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.LOCAL_FILE)
                .scriptReference("inline-script-tenant-aware")
                .build();
        
        // Test with age 20 (passes for regular tenant, fails for premium)
        Customer customer = new Customer("customer-1", 20);
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Execute
        RuleResult result = executor.execute(rule, context, "test-ruleset");
        
        // Verify - should fail because premium tenant requires age 21
        assertThat(result.status()).isEqualTo(RuleStatus.FAILED);
        assertThat(result.tenantId()).isEqualTo("tenant-premium");
    }
    
    @Test
    void testS3ScriptLoadFailure() throws RuleExecutionException {
        // Create rule with S3 script that doesn't exist
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("s3-script-check")
                .name("S3 Script Check")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.S3_OBJECT)
                .scriptReference("s3-script-404")
                .build();
        
        // Create validation context
        Customer customer = new Customer("customer-1", 25);
        ValidationContext<Customer> context = new ValidationContext<>(customer, Map.of());
        
        // Execute
        RuleResult result = executor.execute(rule, context, "test-ruleset");
        
        // Verify - should return ERROR status
        assertThat(result.status()).isEqualTo(RuleStatus.ERROR);
        assertThat(result.message()).contains("Script not found in S3");
        assertThat(result.tenantId()).isEqualTo("tenant-123");
        assertThat(result.ruleCode()).isEqualTo("s3-script-check");
        assertThat(result.error()).isNotNull();
    }
    
    @Test
    void testSupports() {
        GroovyScriptRule rule = GroovyScriptRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("test")
                .name("Test")
                .severity(Severity.ERROR)
                .scriptLocationType(ScriptLocationType.INLINE)
                .scriptReference("test")
                .build();
        
        assertThat(executor.supports(rule)).isTrue();
        
        // Expression rule should not be supported
        ExpressionRule exprRule = ExpressionRule.builder()
                .id(UUID.randomUUID())
                .tenantId("tenant-123")
                .ruleCode("test")
                .name("Test")
                .expression("true")
                .build();
        
        assertThat(executor.supports(exprRule)).isFalse();
    }
    
    // Helper class for testing
    private record Customer(String id, Integer age) {
    }
}

