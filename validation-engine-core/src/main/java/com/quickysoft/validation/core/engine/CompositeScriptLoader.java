package com.quickysoft.validation.core.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite script loader that delegates to multiple script loaders.
 * 
 * Tries each loader in order until one supports the location.
 */
public class CompositeScriptLoader implements ScriptLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(CompositeScriptLoader.class);
    
    private final List<ScriptLoader> loaders = new ArrayList<>();
    
    /**
     * Adds a script loader to the composite.
     */
    public void addLoader(ScriptLoader loader) {
        if (loader != null) {
            loaders.add(loader);
        }
    }
    
    @Override
    public String loadScript(String tenantId, String location) throws ScriptLoadException {
        for (ScriptLoader loader : loaders) {
            if (loader.supports(location)) {
                logger.debug("Loading script with {}: {}", loader.getClass().getSimpleName(), location);
                return loader.loadScript(tenantId, location);
            }
        }
        throw new ScriptLoadException("No script loader supports location: " + location);
    }
    
    @Override
    public boolean supports(String location) {
        return loaders.stream().anyMatch(loader -> loader.supports(location));
    }
}

