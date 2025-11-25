package com.quickysoft.validation.core.model;

/**
 * Type of script location for Groovy script rules.
 */
public enum ScriptLocationType {
    /**
     * Script is stored in a local file system path.
     */
    LOCAL_FILE,
    
    /**
     * Script is stored in an S3 bucket (object key).
     */
    S3_OBJECT,
    
    /**
     * Script is stored inline (e.g., in database or cache).
     */
    INLINE
}

