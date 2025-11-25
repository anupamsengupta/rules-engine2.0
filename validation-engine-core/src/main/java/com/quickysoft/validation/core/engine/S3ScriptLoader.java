package com.quickysoft.validation.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Script loader for S3 object keys.
 * 
 * Supports tenant-scoped script paths and in-memory caching of script content.
 * Uses AWS SDK v2 for S3 operations.
 */
public class S3ScriptLoader implements ScriptLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ScriptLoader.class);
    
    // In-memory cache: tenantId:scriptReference -> script content
    private final Map<String, String> scriptCache = new ConcurrentHashMap<>();
    
    // AWS S3 client (would be injected in production)
    private Object s3Client; // Placeholder - would be S3Client or S3AsyncClient
    
    private final String bucketName;
    private final String basePrefix;
    private final boolean tenantScoped;
    
    /**
     * Creates an S3 script loader.
     * 
     * @param bucketName the S3 bucket name
     * @param basePrefix the base prefix for script keys (e.g., "scripts/")
     * @param tenantScoped whether to use tenant-scoped paths
     */
    public S3ScriptLoader(String bucketName, String basePrefix, boolean tenantScoped) {
        this.bucketName = bucketName;
        this.basePrefix = basePrefix != null ? basePrefix : "";
        this.tenantScoped = tenantScoped;
    }
    
    /**
     * Creates an S3 script loader with default settings.
     */
    public S3ScriptLoader() {
        this(null, null, false);
    }
    
    @Override
    public String loadScript(String tenantId, String location) throws ScriptLoadException {
        try {
            // Check cache first
            String cacheKey = generateCacheKey(tenantId, location);
            String cached = scriptCache.get(cacheKey);
            if (cached != null) {
                logger.debug("Script found in cache for tenant {}: {}", tenantId, location);
                return cached;
            }
            
            // Generate S3 key
            String s3Key = generateS3Key(tenantId, location);
            logger.debug("Loading script from S3 for tenant {}: bucket={}, key={}", tenantId, bucketName, s3Key);
            
            // Load from S3
            String scriptContent = loadFromS3(s3Key);
            
            // Cache the content
            scriptCache.put(cacheKey, scriptContent);
            
            return scriptContent;
        } catch (Exception e) {
            logger.error("Failed to load script from S3 for tenant {}: {}", tenantId, location, e);
            throw new ScriptLoadException(
                    String.format("Failed to load script from S3: tenantId=%s, location=%s, error=%s",
                            tenantId, location, e.getMessage()),
                    e
            );
        }
    }
    
    @Override
    public boolean supports(String location) {
        // Support S3 URLs (s3://bucket/key) or keys that don't look like file paths
        return location != null && 
               (location.startsWith("s3://") || 
                (!location.startsWith("/") && 
                 !location.startsWith("\\") && 
                 location.contains("/") &&
                 !location.contains("\\")));
    }
    
    /**
     * Generates the S3 key for a script location.
     * 
     * If tenantScoped is true, the path will be: {basePrefix}/{tenantId}/{location}
     * Otherwise: {basePrefix}/{location}
     */
    private String generateS3Key(String tenantId, String location) {
        StringBuilder key = new StringBuilder();
        
        if (basePrefix != null && !basePrefix.isEmpty()) {
            key.append(basePrefix);
            if (!basePrefix.endsWith("/")) {
                key.append("/");
            }
        }
        
        if (tenantScoped && tenantId != null) {
            key.append(tenantId).append("/");
        }
        
        // Remove s3:// prefix if present
        if (location.startsWith("s3://")) {
            location = location.substring(5);
            int slashIndex = location.indexOf('/');
            if (slashIndex > 0) {
                location = location.substring(slashIndex + 1);
            }
        }
        
        key.append(location);
        
        return key.toString();
    }
    
    /**
     * Loads script content from S3.
     * 
     * This is a placeholder implementation. In production, this would use AWS SDK v2:
     * 
     * <pre>{@code
     * S3Client s3Client = S3Client.builder()
     *     .region(Region.of(region))
     *     .build();
     * 
     * GetObjectRequest request = GetObjectRequest.builder()
     *     .bucket(bucketName)
     *     .key(s3Key)
     *     .build();
     * 
     * try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
     *     return new String(response.readAllBytes(), StandardCharsets.UTF_8);
     * } catch (NoSuchKeyException e) {
     *     throw new ScriptLoadException("Script not found in S3: " + s3Key, e);
     * }
     * }</pre>
     */
    private String loadFromS3(String s3Key) throws ScriptLoadException {
        if (bucketName == null) {
            throw new ScriptLoadException("S3 bucket name not configured");
        }
        
        // Placeholder - would use AWS SDK v2
        // For now, throw an exception indicating S3 is not fully implemented
        throw new ScriptLoadException(
                String.format("S3 script loading not fully implemented. " +
                        "Please configure AWS SDK v2 S3Client. " +
                        "Bucket: %s, Key: %s", bucketName, s3Key)
        );
    }
    
    /**
     * Generates a cache key for tenant and script location.
     */
    private String generateCacheKey(String tenantId, String location) {
        return tenantId + ":" + location;
    }
    
    /**
     * Evicts a script from the cache.
     */
    public void evictFromCache(String tenantId, String location) {
        String cacheKey = generateCacheKey(tenantId, location);
        scriptCache.remove(cacheKey);
        logger.debug("Evicted script from cache: {}", cacheKey);
    }
    
    /**
     * Clears all cached scripts for a tenant.
     */
    public void evictTenant(String tenantId) {
        scriptCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
        logger.debug("Evicted all scripts for tenant: {}", tenantId);
    }
    
    /**
     * Clears the entire cache.
     */
    public void clearCache() {
        scriptCache.clear();
        logger.debug("Cleared S3 script cache");
    }
}
