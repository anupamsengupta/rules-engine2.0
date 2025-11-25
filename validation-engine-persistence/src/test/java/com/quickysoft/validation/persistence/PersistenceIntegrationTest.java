package com.quickysoft.validation.persistence;

import com.quickysoft.validation.core.model.*;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.entity.RuleEntity;
import com.quickysoft.validation.persistence.entity.RuleSetEntity;
import com.quickysoft.validation.persistence.entity.RuleType;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.provider.JpaRuleSetProvider;
import com.quickysoft.validation.persistence.repository.RuleRepository;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for persistence layer.
 * Tests JPA operations and Redis caching.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PersistenceIntegrationTest {
    
    @Autowired
    private RuleSetRepository ruleSetRepository;
    
    @Autowired
    private RuleRepository ruleRepository;
    
    @Autowired
    private RuleSetMapper mapper;
    
    @Autowired
    private RuleSetCache cache;
    
    @Autowired
    private JpaRuleSetProvider provider;
    
    private String tenantId = "tenant-123";
    private String ruleSetCode = "customer-onboarding";
    private String version = "1.0";
    
    @BeforeEach
    void setUp() {
        // Clear cache before each test
        if (cache != null) {
            cache.evictRuleSet(tenantId, ruleSetCode, version);
        }
    }
    
    @Test
    void testSaveAndLoadRuleSet() {
        // Create rule set entity
        RuleSetEntity ruleSetEntity = new RuleSetEntity();
        ruleSetEntity.setTenantId(tenantId);
        ruleSetEntity.setCode(ruleSetCode);
        ruleSetEntity.setName("Customer Onboarding Rules");
        ruleSetEntity.setDescription("Rules for customer onboarding");
        ruleSetEntity.setVersion(version);
        ruleSetEntity.setActive(true);
        
        // Create expression rule entity
        RuleEntity expressionRule = new RuleEntity();
        expressionRule.setTenantId(tenantId);
        expressionRule.setRuleSet(ruleSetEntity);
        expressionRule.setRuleCode("age-check");
        expressionRule.setName("Age Validation");
        expressionRule.setDescription("Check if customer is 18 or older");
        expressionRule.setPriority(1);
        expressionRule.setEnabled(true);
        expressionRule.setSeverity(Severity.ERROR);
        expressionRule.setRuleType(RuleType.EXPRESSION);
        expressionRule.setExpression("payload.age >= 18");
        
        // Create Groovy script rule entity
        RuleEntity groovyRule = new RuleEntity();
        groovyRule.setTenantId(tenantId);
        groovyRule.setRuleSet(ruleSetEntity);
        groovyRule.setRuleCode("country-check");
        groovyRule.setName("Country Validation");
        groovyRule.setDescription("Check if country is allowed");
        groovyRule.setPriority(2);
        groovyRule.setEnabled(true);
        groovyRule.setSeverity(Severity.WARN);
        groovyRule.setRuleType(RuleType.GROOVY);
        groovyRule.setScriptLocationType(ScriptLocationType.INLINE);
        groovyRule.setScriptReference("return context.get('country') in ['DE', 'US', 'UK']");
        
        ruleSetEntity.setRules(List.of(expressionRule, groovyRule));
        
        // Save
        RuleSetEntity saved = ruleSetRepository.save(ruleSetEntity);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRules()).hasSize(2);
        
        // Load and convert to domain model
        RuleSetEntity loaded = ruleSetRepository.findByTenantIdAndCodeAndVersion(
                tenantId, ruleSetCode, version
        ).orElseThrow();
        
        RuleSet domain = mapper.toDomain(loaded);
        assertThat(domain).isNotNull();
        assertThat(domain.tenantId()).isEqualTo(tenantId);
        assertThat(domain.code()).isEqualTo(ruleSetCode);
        assertThat(domain.version()).isEqualTo(version);
        assertThat(domain.rules()).hasSize(2);
        
        // Verify expression rule
        Rule exprRule = domain.rules().get(0);
        assertThat(exprRule).isInstanceOf(ExpressionRule.class);
        assertThat(exprRule.ruleCode()).isEqualTo("age-check");
        ExpressionRule expr = (ExpressionRule) exprRule;
        assertThat(expr.expression()).isEqualTo("payload.age >= 18");
        
        // Verify Groovy rule
        Rule groovy = domain.rules().get(1);
        assertThat(groovy).isInstanceOf(GroovyScriptRule.class);
        assertThat(groovy.ruleCode()).isEqualTo("country-check");
        GroovyScriptRule groovyScript = (GroovyScriptRule) groovy;
        assertThat(groovyScript.scriptLocationType()).isEqualTo(ScriptLocationType.INLINE);
    }
    
    @Test
    void testProviderWithCache() {
        // Create and save rule set
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(tenantId);
        entity.setCode(ruleSetCode);
        entity.setName("Test Rules");
        entity.setVersion(version);
        entity.setActive(true);
        
        RuleEntity rule = new RuleEntity();
        rule.setTenantId(tenantId);
        rule.setRuleSet(entity);
        rule.setRuleCode("test-rule");
        rule.setName("Test Rule");
        rule.setPriority(1);
        rule.setEnabled(true);
        rule.setSeverity(Severity.ERROR);
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setExpression("true");
        
        entity.setRules(List.of(rule));
        ruleSetRepository.save(entity);
        
        // First call - should hit JPA and cache
        RuleSet first = provider.getRuleSet(tenantId, ruleSetCode, version);
        assertThat(first).isNotNull();
        assertThat(first.code()).isEqualTo(ruleSetCode);
        
        // Delete from JPA to verify cache is used
        ruleSetRepository.delete(entity);
        
        // Second call - should hit cache (not JPA)
        RuleSet cached = provider.getRuleSet(tenantId, ruleSetCode, version);
        assertThat(cached).isNotNull();
        assertThat(cached.code()).isEqualTo(ruleSetCode);
        
        // Evict and verify it's gone
        provider.evictRuleSet(tenantId, ruleSetCode, version);
        RuleSet afterEvict = provider.getRuleSet(tenantId, ruleSetCode, version);
        assertThat(afterEvict).isNull();
    }
    
    @Test
    void testMetadataAndApplicableContexts() {
        RuleSetEntity entity = new RuleSetEntity();
        entity.setTenantId(tenantId);
        entity.setCode(ruleSetCode);
        entity.setName("Test");
        entity.setVersion(version);
        entity.setActive(true);
        
        RuleEntity rule = new RuleEntity();
        rule.setTenantId(tenantId);
        rule.setRuleSet(entity);
        rule.setRuleCode("test");
        rule.setName("Test");
        rule.setPriority(1);
        rule.setEnabled(true);
        rule.setSeverity(Severity.ERROR);
        rule.setRuleType(RuleType.EXPRESSION);
        rule.setExpression("true");
        rule.setMetadata(Map.of("key1", "value1", "key2", "value2"));
        rule.setApplicableContexts("[\"context1\", \"context2\"]");
        
        entity.setRules(List.of(rule));
        ruleSetRepository.save(entity);
        
        RuleSet domain = mapper.toDomain(ruleSetRepository.findByTenantIdAndCodeAndVersion(
                tenantId, ruleSetCode, version
        ).orElseThrow());
        
        Rule loadedRule = domain.rules().get(0);
        assertThat(loadedRule.metadata()).containsEntry("key1", "value1");
        assertThat(loadedRule.metadata()).containsEntry("key2", "value2");
        assertThat(loadedRule.applicableContexts()).contains("context1", "context2");
    }
    
    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public RuleSetMapper ruleSetMapper() {
            return new RuleSetMapper();
        }
        
        @Bean
        public RedisTemplate<String, String> ruleSetRedisTemplate() {
            // Use in-memory Redis connection for testing
            LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
            connectionFactory.afterPropertiesSet();
            
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            template.afterPropertiesSet();
            return template;
        }
        
        @Bean
        public RuleSetCache ruleSetCache(RedisTemplate<String, String> redisTemplate) {
            return new com.quickysoft.validation.persistence.cache.RedisRuleSetCache(
                    redisTemplate, new com.fasterxml.jackson.databind.ObjectMapper()
            );
        }
        
        @Bean
        public JpaRuleSetProvider jpaRuleSetProvider(
                RuleSetCache cache,
                RuleSetRepository repository,
                RuleSetMapper mapper
        ) {
            return new JpaRuleSetProvider(cache, repository, mapper);
        }
    }
}

