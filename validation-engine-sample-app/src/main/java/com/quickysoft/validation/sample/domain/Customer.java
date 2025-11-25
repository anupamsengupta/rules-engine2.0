package com.quickysoft.validation.sample.domain;

/**
 * Sample domain class for customer validation.
 */
public record Customer(
        String id,
        String name,
        Integer age,
        String email,
        String country,
        String channel
) {
}

