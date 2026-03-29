package com.knubisoft.comparator.condition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link BigDecimalComparator} verifying decimal comparison across all operators. */
class BigDecimalComparatorTest {

    private final BigDecimalComparator comparator = new BigDecimalComparator();

    @Test
    void moreThan() {
        assertTrue(comparator.compare("10.5", "5.5", Operator.MORE_THEN));
        assertFalse(comparator.compare("5.5", "10.5", Operator.MORE_THEN));
        assertFalse(comparator.compare("5.0", "5.0", Operator.MORE_THEN));
    }

    @Test
    void lessThan() {
        assertTrue(comparator.compare("3.14", "10.5", Operator.LESS_THEN));
        assertFalse(comparator.compare("10.5", "3.14", Operator.LESS_THEN));
    }

    @Test
    void moreThanOrEqual() {
        assertTrue(comparator.compare("10.5", "5.5", Operator.MORE_THEN_OR_EQUAL));
        assertTrue(comparator.compare("5.5", "5.5", Operator.MORE_THEN_OR_EQUAL));
        assertFalse(comparator.compare("3.0", "5.5", Operator.MORE_THEN_OR_EQUAL));
    }

    @Test
    void lessThanOrEqual() {
        assertTrue(comparator.compare("3.14", "5.5", Operator.LESS_THEN_OR_EQUAL));
        assertTrue(comparator.compare("5.5", "5.5", Operator.LESS_THEN_OR_EQUAL));
        assertFalse(comparator.compare("10.5", "5.5", Operator.LESS_THEN_OR_EQUAL));
    }

    @Test
    void negativeDecimals() {
        assertTrue(comparator.compare("-1.5", "-5.5", Operator.MORE_THEN));
        assertTrue(comparator.compare("-10.5", "-3.14", Operator.LESS_THEN));
    }

    @Test
    void integerStringsWorkToo() {
        assertTrue(comparator.compare("10", "5", Operator.MORE_THEN));
    }
}
