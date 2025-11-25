package com.quickysoft.validation.persistence.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickysoft.validation.core.model.RuleSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis/ElastiCache-based implementation of RuleSetCache.
 * 
 * Uses JSON serialization for rule sets.
 * Cache key pattern: ruleset:{tenantId}:{ruleSetCode}:{version}
 */
@Component
public class RedisRuleSetCache implements RuleSetCache {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisRuleSetCache.class);
    private static final String CACHE_KEY_PREFIX = "ruleset";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisRuleSetCache(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }
    
    @Override
    public RuleSet getRuleSet(String tenantId, String ruleSetCode, String version) {
        String key = generateCacheKey(tenantId, ruleSetCode, version);
        
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                logger.debug("Rule set not found in Redis cache: {}", key);
                return null;
            }
            
            logger.debug("Rule set found in Redis cache: {}", key);
            return objectMapper.readValue(json, RuleSet.class);
        } catch (Exception e) {
            logger.error("Error deserializing rule set from Redis: {}", key, e);
            return null;
        }
    }
    
    @Override
    public void putRuleSet(RuleSet ruleSet) {
        putRuleSet(ruleSet, DEFAULT_TTL);
    }
    
    /**
     * Caches a rule set with a custom TTL.
     */
    public void putRuleSet(RuleSet ruleSet, Duration ttl) {
        String key = generateCacheKey(
                ruleSet.tenantId(),
                ruleSet.code(),
                ruleSet.version()
        );
        
        try {
            String json = objectMapper.writeValueAsString(ruleSet);
            redisTemplate.opsForValue().set(key, json, ttl);
            logger.debug("Cached rule set in Redis: {} (TTL: {})", key, ttl);
        } catch (Exception e) {
            logger.error("Error serializing rule set to Redis: {}", key, e);
        }
    }
    
    @Override
    public void evictRuleSet(String tenantId, String ruleSetCode, String version) {
        String key = generateCacheKey(tenantId, ruleSetCode, version);
        redisTemplate.delete(key);
        logger.debug("Evicted rule set from Redis cache: {}", key);
    }
    
    /**
     * Generates a cache key for a rule set.
     */
    private String generateCacheKey(String tenantId, String ruleSetCode, String version) {
        return String.format("%s:%s:%s:%s", CACHE_KEY_PREFIX, tenantId, ruleSetCode, version);
    }
}

