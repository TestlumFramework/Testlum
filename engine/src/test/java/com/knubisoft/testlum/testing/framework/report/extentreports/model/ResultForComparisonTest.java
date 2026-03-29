package com.knubisoft.testlum.testing.framework.report.extentreports.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultForComparisonTest {

    @Test
    void constructorShouldSetExpectedAndActual() {
        ResultForComparison result = new ResultForComparison("expectedValue", "actualValue");

        assertEquals("expectedValue", result.getExpected());
        assertEquals("actualValue", result.getActual());
    }

    @Test
    void shouldHandleNullValues() {
        ResultForComparison result = new ResultForComparison(null, null);

        assertNull(result.getExpected());
        assertNull(result.getActual());
    }

    @Test
    void shouldHandleEmptyStrings() {
        ResultForComparison result = new ResultForComparison("", "");

        assertEquals("", result.getExpected());
        assertEquals("", result.getActual());
    }

    @Test
    void constructorShouldPreserveExpectedValue() {
        ResultForComparison result = new ResultForComparison("initial", "actual");

        assertEquals("initial", result.getExpected());
    }

    @Test
    void constructorShouldPreserveActualValue() {
        ResultForComparison result = new ResultForComparison("expected", "initial");

        assertEquals("initial", result.getActual());
    }
}
