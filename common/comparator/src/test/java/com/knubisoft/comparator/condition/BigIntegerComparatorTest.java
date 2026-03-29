package com.knubisoft.comparator.condition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link BigIntegerComparator} verifying integer comparison across all operators. */
class BigIntegerComparatorTest {

    private final BigIntegerComparator comparator = new BigIntegerComparator();

    @Test
    void moreThan() {
        assertTrue(comparator.compare("10", "5", Operator.MORE_THEN));
        assertFalse(comparator.compare("5", "10", Operator.MORE_THEN));
        assertFalse(comparator.compare("5", "5", Operator.MORE_THEN));
    }

    @Test
    void lessThan() {
        assertTrue(comparator.compare("3", "10", Operator.LESS_THEN));
        assertFalse(comparator.compare("10", "3", Operator.LESS_THEN));
        assertFalse(comparator.compare("5", "5", Operator.LESS_THEN));
    }

    @Test
    void moreThanOrEqual() {
        assertTrue(comparator.compare("10", "5", Operator.MORE_THEN_OR_EQUAL));
        assertTrue(comparator.compare("5", "5", Operator.MORE_THEN_OR_EQUAL));
        assertFalse(comparator.compare("3", "5", Operator.MORE_THEN_OR_EQUAL));
    }

    @Test
    void lessThanOrEqual() {
        assertTrue(comparator.compare("3", "5", Operator.LESS_THEN_OR_EQUAL));
        assertTrue(comparator.compare("5", "5", Operator.LESS_THEN_OR_EQUAL));
        assertFalse(comparator.compare("10", "5", Operator.LESS_THEN_OR_EQUAL));
    }

    @Test
    void negativeNumbers() {
        assertTrue(comparator.compare("-1", "-5", Operator.MORE_THEN));
        assertTrue(comparator.compare("-10", "-3", Operator.LESS_THEN));
    }

    @Test
    void largeNumbers() {
        assertTrue(comparator.compare("999999999999999999", "1", Operator.MORE_THEN));
    }
}
