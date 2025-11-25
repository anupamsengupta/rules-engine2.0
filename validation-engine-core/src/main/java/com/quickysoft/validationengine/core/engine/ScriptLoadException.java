package com.quickysoft.validationengine.core.engine;

/**
 * Exception thrown when script loading fails.
 */
public class ScriptLoadException extends Exception {
    
    public ScriptLoadException(String message) {
        super(message);
    }
    
    public ScriptLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

