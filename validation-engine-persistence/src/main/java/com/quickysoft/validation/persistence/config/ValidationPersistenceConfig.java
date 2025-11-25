package com.quickysoft.validation.persistence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.cache.RedisRuleSetCache;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.provider.JpaRuleSetProvider;
import com.quickysoft.validation.persistence.repository.RuleRepository;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Auto-configuration for validation engine persistence layer.
 * 
 * Registers:
 * - JPA repositories
 * - Mappers
 * - JpaRuleSetProvider
 * - RedisRuleSetCache
 * - Redis configuration
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.quickysoft.validation.persistence.repository")
public class ValidationPersistenceConfig {
    
    /**
     * Redis template for rule set caching.
     */
    @Bean
    @ConditionalOnMissingBean(name = "ruleSetRedisTemplate")
    public RedisTemplate<String, String> ruleSetRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * Object mapper for JSON serialization.
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
    
    /**
     * Rule set mapper.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleSetMapper ruleSetMapper() {
        return new RuleSetMapper();
    }
    
    /**
     * Redis-based rule set cache.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleSetCache ruleSetCache(
            RedisTemplate<String, String> ruleSetRedisTemplate,
            ObjectMapper objectMapper
    ) {
        return new RedisRuleSetCache(ruleSetRedisTemplate, objectMapper);
    }
    
    /**
     * JPA-based rule set provider with Redis caching.
     */
    @Bean
    @ConditionalOnMissingBean
    public JpaRuleSetProvider jpaRuleSetProvider(
            RuleSetCache ruleSetCache,
            RuleSetRepository ruleSetRepository,
            RuleSetMapper ruleSetMapper
    ) {
        return new JpaRuleSetProvider(ruleSetCache, ruleSetRepository, ruleSetMapper);
    }
}

