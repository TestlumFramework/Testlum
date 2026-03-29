package com.knubisoft.comparator.util;

import com.knubisoft.comparator.condition.Operator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link ConditionalTypeComparator} verifying generic comparable comparison for all four operators. */
class ConditionalTypeComparatorTest {

    @Nested
    class MoreThen {
        @Test
        void returnsTrueWhenActualIsGreater() {
            assertTrue(ConditionalTypeComparator.compareConditions(10, 5, Operator.MORE_THEN));
        }

        @Test
        void returnsFalseWhenActualIsEqual() {
            assertFalse(ConditionalTypeComparator.compareConditions(5, 5, Operator.MORE_THEN));
        }

        @Test
        void returnsFalseWhenActualIsLess() {
            assertFalse(ConditionalTypeComparator.compareConditions(3, 5, Operator.MORE_THEN));
        }
    }

    @Nested
    class LessThen {
        @Test
        void returnsTrueWhenActualIsLess() {
            assertTrue(ConditionalTypeComparator.compareConditions(3, 5, Operator.LESS_THEN));
        }

        @Test
        void returnsFalseWhenActualIsEqual() {
            assertFalse(ConditionalTypeComparator.compareConditions(5, 5, Operator.LESS_THEN));
        }

        @Test
        void returnsFalseWhenActualIsGreater() {
            assertFalse(ConditionalTypeComparator.compareConditions(10, 5, Operator.LESS_THEN));
        }
    }

    @Nested
    class MoreThenOrEqual {
        @Test
        void returnsTrueWhenActualIsGreater() {
            assertTrue(ConditionalTypeComparator.compareConditions(10, 5, Operator.MORE_THEN_OR_EQUAL));
        }

        @Test
        void returnsTrueWhenActualIsEqual() {
            assertTrue(ConditionalTypeComparator.compareConditions(5, 5, Operator.MORE_THEN_OR_EQUAL));
        }

        @Test
        void returnsFalseWhenActualIsLess() {
            assertFalse(ConditionalTypeComparator.compareConditions(3, 5, Operator.MORE_THEN_OR_EQUAL));
        }
    }

    @Nested
    class LessThenOrEqual {
        @Test
        void returnsTrueWhenActualIsLess() {
            assertTrue(ConditionalTypeComparator.compareConditions(3, 5, Operator.LESS_THEN_OR_EQUAL));
        }

        @Test
        void returnsTrueWhenActualIsEqual() {
            assertTrue(ConditionalTypeComparator.compareConditions(5, 5, Operator.LESS_THEN_OR_EQUAL));
        }

        @Test
        void returnsFalseWhenActualIsGreater() {
            assertFalse(ConditionalTypeComparator.compareConditions(10, 5, Operator.LESS_THEN_OR_EQUAL));
        }
    }

    @Test
    void worksWithStrings() {
        assertTrue(ConditionalTypeComparator.compareConditions("b", "a", Operator.MORE_THEN));
        assertTrue(ConditionalTypeComparator.compareConditions("a", "b", Operator.LESS_THEN));
    }
}
