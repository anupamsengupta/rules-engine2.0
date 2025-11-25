package com.quickysoft.validation.core.engine;

/**
 * Loads script content from external sources (file system, S3, etc.).
 */
public interface ScriptLoader {
    
    /**
     * Loads script content from the given location.
     *
     * @param tenantId the tenant identifier (for tenant-aware loading)
     * @param location the script location (path, S3 key, etc.)
     * @return the script content
     * @throws ScriptLoadException if loading fails
     */
    String loadScript(String tenantId, String location) throws ScriptLoadException;
    
    /**
     * Checks if this loader supports the given location format.
     *
     * @param location the location to check
     * @return true if this loader can handle the location
     */
    boolean supports(String location);
}

