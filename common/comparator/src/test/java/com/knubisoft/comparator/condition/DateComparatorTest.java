package com.knubisoft.comparator.condition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link DateComparator} verifying date, datetime, and time comparison across all operators. */
class DateComparatorTest {

    private final DateComparator comparator = new DateComparator();

    @Test
    void compareDateWithDashMoreThan() {
        assertTrue(comparator.compare("2024-06-15", "2024-01-15", Operator.MORE_THEN));
        assertFalse(comparator.compare("2024-01-15", "2024-06-15", Operator.MORE_THEN));
    }

    @Test
    void compareDateWithDashLessThan() {
        assertTrue(comparator.compare("2024-01-15", "2024-06-15", Operator.LESS_THEN));
        assertFalse(comparator.compare("2024-06-15", "2024-01-15", Operator.LESS_THEN));
    }

    @Test
    void compareDateWithDashMoreThanOrEqual() {
        assertTrue(comparator.compare("2024-06-15", "2024-01-15", Operator.MORE_THEN_OR_EQUAL));
        assertTrue(comparator.compare("2024-01-15", "2024-01-15", Operator.MORE_THEN_OR_EQUAL));
        assertFalse(comparator.compare("2024-01-15", "2024-06-15", Operator.MORE_THEN_OR_EQUAL));
    }

    @Test
    void compareDateWithDashLessThanOrEqual() {
        assertTrue(comparator.compare("2024-01-15", "2024-06-15", Operator.LESS_THEN_OR_EQUAL));
        assertTrue(comparator.compare("2024-01-15", "2024-01-15", Operator.LESS_THEN_OR_EQUAL));
        assertFalse(comparator.compare("2024-06-15", "2024-01-15", Operator.LESS_THEN_OR_EQUAL));
    }

    @Test
    void compareDateTimeWithDash() {
        assertTrue(comparator.compare("2024-06-15 10:30:00", "2024-01-15 10:30:00", Operator.MORE_THEN));
        assertTrue(comparator.compare("2024-01-15 10:30:00", "2024-06-15 10:30:00", Operator.LESS_THEN));
    }

    @Test
    void compareTimeOnly() {
        assertTrue(comparator.compare("14:30:00", "10:00:00", Operator.MORE_THEN));
        assertTrue(comparator.compare("10:00:00", "14:30:00", Operator.LESS_THEN));
    }
}
