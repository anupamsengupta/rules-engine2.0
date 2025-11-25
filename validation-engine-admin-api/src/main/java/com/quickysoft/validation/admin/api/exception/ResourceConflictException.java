package com.quickysoft.validation.admin.api.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate key).
 */
public class ResourceConflictException extends RuntimeException {
    
    public ResourceConflictException(String message) {
        super(message);
    }
    
    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

