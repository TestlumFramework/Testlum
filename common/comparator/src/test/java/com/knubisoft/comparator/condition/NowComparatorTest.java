package com.knubisoft.comparator.condition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link NowComparator} verifying comparison of date values against the current time. */
class NowComparatorTest {

    private final NowComparator comparator = new NowComparator();

    @Test
    void pastDateIsLessThanNow() {
        assertTrue(comparator.compare("2000-01-01", "now", Operator.LESS_THEN));
    }

    @Test
    void pastDateIsNotMoreThanNow() {
        assertFalse(comparator.compare("2000-01-01", "now", Operator.MORE_THEN));
    }

    @Test
    void pastDateTimeLessThanNow() {
        assertTrue(comparator.compare("2000-01-01 00:00:00", "now", Operator.LESS_THEN));
    }

    @Test
    void pastDateTimeLessThanOrEqualToNow() {
        assertTrue(comparator.compare("2000-01-01 00:00:00", "now", Operator.LESS_THEN_OR_EQUAL));
    }

    @Test
    void futureDateIsMoreThanNow() {
        assertTrue(comparator.compare("2099-12-31", "now", Operator.MORE_THEN));
    }

    @Test
    void futureDateIsMoreThanOrEqualToNow() {
        assertTrue(comparator.compare("2099-12-31", "now", Operator.MORE_THEN_OR_EQUAL));
    }
}
