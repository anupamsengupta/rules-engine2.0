package com.quickysoft.validation.core.engine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tenant-aware cache for compiled Groovy scripts.
 * 
 * Caches compiled Groovy script classes per tenant and script location.
 * This is independent of Redis and provides fast access to compiled scripts.
 */
public class GroovyScriptCache {
    
    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptCache.class);
    
    private final Map<String, Class<? extends Script>> scriptClassCache = new ConcurrentHashMap<>();
    private final GroovyClassLoader groovyClassLoader;
    
    public GroovyScriptCache() {
        this.groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
    }
    
    /**
     * Gets a cached compiled script class, or compiles and caches it if not found.
     * 
     * @param tenantId the tenant identifier
     * @param scriptLocation the script location (for cache key)
     * @param scriptContent the script content to compile
     * @return the compiled script class
     * @throws RuleExecutionException if compilation fails
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Script> getOrCompile(String tenantId, String scriptLocation, String scriptContent) 
            throws RuleExecutionException {
        String cacheKey = generateCacheKey(tenantId, scriptLocation);
        
        // Check cache first
        Class<? extends Script> cached = scriptClassCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Compile and cache
        try {
            logger.debug("Compiling Groovy script for tenant {}: {}", tenantId, scriptLocation);
            Class<? extends Script> compiled = (Class<? extends Script>) groovyClassLoader.parseClass(scriptContent);
            scriptClassCache.put(cacheKey, compiled);
            return compiled;
        } catch (Exception e) {
            logger.error("Failed to compile Groovy script for tenant {}: {}", tenantId, scriptLocation, e);
            throw new RuleExecutionException(
                    "Failed to compile Groovy script: " + scriptLocation, e
            );
        }
    }
    
    /**
     * Evicts a script from the cache.
     * 
     * @param tenantId the tenant identifier
     * @param scriptLocation the script location
     */
    public void evict(String tenantId, String scriptLocation) {
        String cacheKey = generateCacheKey(tenantId, scriptLocation);
        scriptClassCache.remove(cacheKey);
        logger.debug("Evicted script from cache: {}", cacheKey);
    }
    
    /**
     * Clears all cached scripts for a tenant.
     * 
     * @param tenantId the tenant identifier
     */
    public void evictTenant(String tenantId) {
        scriptClassCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
        logger.debug("Evicted all scripts for tenant: {}", tenantId);
    }
    
    /**
     * Clears the entire cache.
     */
    public void clear() {
        scriptClassCache.clear();
        logger.debug("Cleared all script cache");
    }
    
    /**
     * Generates a cache key for a tenant and script location.
     */
    private String generateCacheKey(String tenantId, String scriptLocation) {
        return tenantId + ":" + scriptLocation;
    }
    
    /**
     * Gets the current cache size.
     */
    public int getCacheSize() {
        return scriptClassCache.size();
    }
}

