package com.quickysoft.validation.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Script loader for local file system paths.
 */
public class FileSystemScriptLoader implements ScriptLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemScriptLoader.class);
    
    @Override
    public String loadScript(String tenantId, String location) throws ScriptLoadException {
        try {
            Path path = Paths.get(location);
            if (!Files.exists(path)) {
                throw new ScriptLoadException("Script file not found: " + location);
            }
            if (!Files.isReadable(path)) {
                throw new ScriptLoadException("Script file is not readable: " + location);
            }
            logger.debug("Loading script from file system for tenant {}: {}", tenantId, location);
            return Files.readString(path);
        } catch (IOException e) {
            throw new ScriptLoadException("Failed to load script from file system: " + location, e);
        }
    }
    
    @Override
    public boolean supports(String location) {
        // Simple heuristic: if it looks like a file path (starts with / or contains path separators)
        return location != null && 
               (location.startsWith("/") || 
                location.startsWith("\\") || 
                location.contains("/") || 
                location.contains("\\"));
    }
}

