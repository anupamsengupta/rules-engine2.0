package com.quickysoft.validation.core.engine.expression;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ExpressionEvaluatorType enum.
 */
class ExpressionEvaluatorTypeTest {
    
    @Test
    void testFromString_UpperCase() {
        assertThat(ExpressionEvaluatorType.fromString("SPEL")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("MVEL")).isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("JEXL")).isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_LowerCase() {
        assertThat(ExpressionEvaluatorType.fromString("spel")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("mvel")).isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("jexl")).isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_MixedCase() {
        assertThat(ExpressionEvaluatorType.fromString("Spel")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("Mvel")).isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("Jexl")).isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_WithWhitespace() {
        assertThat(ExpressionEvaluatorType.fromString("  SPEL  ")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("  MVEL  ")).isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("  JEXL  ")).isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_Null() {
        assertThat(ExpressionEvaluatorType.fromString(null)).isEqualTo(ExpressionEvaluatorType.SPEL);
    }
    
    @Test
    void testFromString_Empty() {
        assertThat(ExpressionEvaluatorType.fromString("")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("   ")).isEqualTo(ExpressionEvaluatorType.SPEL);
    }
    
    @Test
    void testFromString_InvalidValue() {
        assertThat(ExpressionEvaluatorType.fromString("INVALID")).isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("UNKNOWN")).isEqualTo(ExpressionEvaluatorType.SPEL);
    }
    
    @Test
    void testFromString_WithDefault() {
        assertThat(ExpressionEvaluatorType.fromString("SPEL", ExpressionEvaluatorType.MVEL))
                .isEqualTo(ExpressionEvaluatorType.SPEL);
        assertThat(ExpressionEvaluatorType.fromString("MVEL", ExpressionEvaluatorType.SPEL))
                .isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("JEXL", ExpressionEvaluatorType.SPEL))
                .isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_NullWithDefault() {
        assertThat(ExpressionEvaluatorType.fromString(null, ExpressionEvaluatorType.MVEL))
                .isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString(null, ExpressionEvaluatorType.JEXL))
                .isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_InvalidWithDefault() {
        assertThat(ExpressionEvaluatorType.fromString("INVALID", ExpressionEvaluatorType.MVEL))
                .isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("UNKNOWN", ExpressionEvaluatorType.JEXL))
                .isEqualTo(ExpressionEvaluatorType.JEXL);
    }
    
    @Test
    void testFromString_EmptyWithDefault() {
        assertThat(ExpressionEvaluatorType.fromString("", ExpressionEvaluatorType.MVEL))
                .isEqualTo(ExpressionEvaluatorType.MVEL);
        assertThat(ExpressionEvaluatorType.fromString("   ", ExpressionEvaluatorType.JEXL))
                .isEqualTo(ExpressionEvaluatorType.JEXL);
    }
}

