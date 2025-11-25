package com.quickysoft.validationengine.persistence.cache;

import com.quickysoft.validationengine.core.domain.RuleSet;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Service for caching rule sets in Redis.
 * Uses tenant-aware cache keys: ruleset:{tenantId}:{ruleSetCode}:{version}
 */
@Service
public class RedisCacheService {
    
    private static final String CACHE_KEY_PREFIX = "ruleset";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RuleSetSerializer ruleSetSerializer;
    
    public RedisCacheService(
            RedisTemplate<String, Object> redisTemplate,
            RuleSetSerializer ruleSetSerializer
    ) {
        this.redisTemplate = redisTemplate;
        this.ruleSetSerializer = ruleSetSerializer;
    }
    
    /**
     * Generates a cache key for a rule set.
     */
    public String generateCacheKey(String tenantId, String ruleSetCode, String version) {
        return String.format("%s:%s:%s:%s", CACHE_KEY_PREFIX, tenantId, ruleSetCode, version);
    }
    
    /**
     * Retrieves a rule set from cache.
     */
    public Optional<RuleSet> get(String tenantId, String ruleSetCode, String version) {
        String key = generateCacheKey(tenantId, ruleSetCode, version);
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return ruleSetSerializer.deserialize(value);
    }
    
    /**
     * Caches a rule set.
     */
    public void put(RuleSet ruleSet) {
        put(ruleSet, DEFAULT_TTL);
    }
    
    /**
     * Caches a rule set with a custom TTL.
     */
    public void put(RuleSet ruleSet, Duration ttl) {
        String key = generateCacheKey(
                ruleSet.tenantId(),
                ruleSet.ruleSetCode(),
                ruleSet.version()
        );
        Object serialized = ruleSetSerializer.serialize(ruleSet);
        redisTemplate.opsForValue().set(key, serialized, ttl);
    }
    
    /**
     * Evicts a rule set from cache.
     */
    public void evict(String tenantId, String ruleSetCode, String version) {
        String key = generateCacheKey(tenantId, ruleSetCode, version);
        redisTemplate.delete(key);
    }
}

