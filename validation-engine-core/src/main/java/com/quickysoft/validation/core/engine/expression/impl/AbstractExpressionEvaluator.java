package com.quickysoft.validation.core.engine.expression.impl;

import com.quickysoft.validation.core.engine.expression.ExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExpressionEvaluator implements ExpressionEvaluator {
    /**
     * Builds a nested Map from flat Map<String, Object> where keys with dot notation (e.g., "order.currency")
     * are resolved into nested structures (e.g., context.get("order").get("currency")).
     */
    protected Map<String, Object> buildNestedContext(Map<String, Object> flatAttributes) {
        Map<String, Object> nested = new HashMap<>();
        if (flatAttributes != null) {
            for (Map.Entry<String, Object> entry : flatAttributes.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                setNestedValue(nested, key.split("\\."), value);
            }
        }
        return nested;
    }

    /**
     * Recursively sets a value in a nested Map using the path segments.
     */
    @SuppressWarnings("unchecked")
    protected void setNestedValue(Map<String, Object> root, String[] path, Object value) {
        Map<String, Object> current = root;
        for (int i = 0; i < path.length - 1; i++) {
            String segment = path[i];
            if (!current.containsKey(segment)) {
                current.put(segment, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(segment);
        }
        current.put(path[path.length - 1], value);
    }
}
