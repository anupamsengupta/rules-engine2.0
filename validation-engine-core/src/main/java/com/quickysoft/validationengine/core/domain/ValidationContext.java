package com.quickysoft.validationengine.core.domain;

import java.util.Map;

/**
 * Context for rule evaluation, containing the input payload and contextual attributes.
 */
public record ValidationContext(
        Object input,
        Map<String, Object> attributes
) {
    public ValidationContext {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null");
        }
        if (attributes == null) {
            attributes = Map.of();
        }
    }
    
    public ValidationContext(Object input) {
        this(input, Map.of());
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getInput(Class<T> type) {
        return type.cast(input);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return value != null ? type.cast(value) : null;
    }
}

