package com.quickysoft.validation.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickysoft.validation.core.engine.*;
import com.quickysoft.validation.core.provider.RuleSetProvider;
import com.quickysoft.validation.persistence.cache.NoOpRuleSetCache;
import com.quickysoft.validation.persistence.cache.RuleSetCache;
import com.quickysoft.validation.persistence.cache.RedisRuleSetCache;
import com.quickysoft.validation.persistence.mapper.RuleSetMapper;
import com.quickysoft.validation.persistence.provider.JpaRuleSetProvider;
import com.quickysoft.validation.persistence.repository.RuleSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for Validation Engine.
 * 
 * Automatically configures ValidationEngine and all its dependencies when
 * validation-engine-persistence is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass(ValidationEngine.class)
@EnableConfigurationProperties(ValidationEngineProperties.class)
public class ValidationEngineAutoConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationEngineAutoConfiguration.class);
    
    /**
     * Redis template for rule set caching (if Redis is available).
     */
    @Bean
    @ConditionalOnMissingBean(name = "ruleSetRedisTemplate")
    @ConditionalOnClass(RedisConnectionFactory.class)
    public RedisTemplate<String, String> ruleSetRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        logger.info("Configured Redis template for rule set caching");
        return template;
    }
    
    /**
     * Object mapper for JSON serialization.
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper validationEngineObjectMapper() {
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
     * Redis-based rule set cache (if Redis is available).
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnProperty(name = "quickysoft.validation.cache.enabled", havingValue = "true", matchIfMissing = true)
    public RuleSetCache redisRuleSetCache(
            RedisTemplate<String, String> ruleSetRedisTemplate,
            ObjectMapper objectMapper
    ) {
        logger.info("Using Redis-based rule set cache");
        return new RedisRuleSetCache(ruleSetRedisTemplate, objectMapper);
    }
    
    /**
     * No-op rule set cache (when Redis is not available or disabled).
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleSetCache noOpRuleSetCache() {
        logger.info("Using NoOp rule set cache (Redis not available or disabled)");
        return new NoOpRuleSetCache();
    }
    
    /**
     * JPA-based rule set provider with caching.
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
    
    /**
     * Script loader registry that delegates to multiple loaders.
     */
    @Bean
    @ConditionalOnMissingBean
    public ScriptLoader scriptLoader(ValidationEngineProperties properties) {
        CompositeScriptLoader loader = new CompositeScriptLoader();
        
        // Add file system loader
        loader.addLoader(new FileSystemScriptLoader());
        
        // Add S3 loader if configured
        if (properties.getScript().getS3() != null && properties.getScript().getS3().isEnabled()) {
            S3ScriptLoader s3Loader = new S3ScriptLoader();
            // TODO: Configure S3 client with properties
            loader.addLoader(s3Loader);
            logger.info("S3 script loader enabled (bucket: {})", properties.getScript().getS3().getBucket());
        }
        
        return loader;
    }
    
    /**
     * Groovy script cache for compiled scripts.
     */
    @Bean
    @ConditionalOnMissingBean
    public GroovyScriptCache groovyScriptCache() {
        return new GroovyScriptCache();
    }
    
    /**
     * Expression rule executor.
     */
    @Bean
    @ConditionalOnMissingBean
    public ExpressionRuleExecutor expressionRuleExecutor() {
        return new ExpressionRuleExecutor();
    }
    
    /**
     * Groovy script rule executor.
     */
    @Bean
    @ConditionalOnMissingBean
    public GroovyScriptRuleExecutor groovyScriptRuleExecutor(
            ScriptLoader scriptLoader,
            GroovyScriptCache scriptCache
    ) {
        return new GroovyScriptRuleExecutor(scriptLoader, scriptCache);
    }
    
    /**
     * Rule set result calculator.
     */
    @Bean
    @ConditionalOnMissingBean
    public RuleSetResultCalculator ruleSetResultCalculator() {
        return new RuleSetResultCalculator();
    }
    
    /**
     * Validation engine - the main bean that applications will use.
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidationEngine validationEngine(
            RuleSetProvider ruleSetProvider,
            ExpressionRuleExecutor expressionRuleExecutor,
            GroovyScriptRuleExecutor groovyScriptRuleExecutor,
            RuleSetResultCalculator resultCalculator
    ) {
        List<RuleExecutor> executors = new ArrayList<>();
        executors.add(expressionRuleExecutor);
        executors.add(groovyScriptRuleExecutor);
        
        logger.info("Auto-configured ValidationEngine with {} rule executors", executors.size());
        return new DefaultValidationEngine(ruleSetProvider, executors, resultCalculator);
    }
}

