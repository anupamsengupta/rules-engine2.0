package com.quickysoft.validation.persistence.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA converter for Map<String, String> metadata to JSON.
 */
@Converter
public class MetadataConverter implements AttributeConverter<Map<String, String>, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(MetadataConverter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            logger.error("Failed to convert metadata to JSON", e);
            return null;
        }
    }
    
    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            logger.error("Failed to convert JSON to metadata", e);
            return new HashMap<>();
        }
    }
}

