package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ConditionType} enum verifying expression
 * classification into INTEGER, DECIMAL, DATE, and NOW types.
 */
class ConditionTypeTest {

    @ParameterizedTest
    @CsvSource({
            ">100, INTEGER",
            "<50, INTEGER",
            ">=200, INTEGER",
            "<=10, INTEGER",
            ">-5, INTEGER",
    })
    void getTypeReturnsIntegerForIntExpressions(final String expression, final String expectedType) {
        assertEquals(ConditionType.valueOf(expectedType), ConditionType.getType(expression));
    }

    @ParameterizedTest
    @CsvSource({
            ">100.5, DECIMAL",
            "<3.14, DECIMAL",
            ">=0.01, DECIMAL",
            "<=-99.9, DECIMAL",
    })
    void getTypeReturnsDecimalForDecExpressions(final String expression, final String expectedType) {
        assertEquals(ConditionType.valueOf(expectedType), ConditionType.getType(expression));
    }

    @ParameterizedTest
    @CsvSource({
            ">2024-01-15, DATE",
            "<2024/06/30, DATE",
            ">=2024-01-15 10:30:00, DATE",
            "<=2024/06/30 23:59:59, DATE",
            ">10:30:00, DATE",
            "<23:59:59, DATE",
    })
    void getTypeReturnsDateForDateExpressions(final String expression, final String expectedType) {
        assertEquals(ConditionType.valueOf(expectedType), ConditionType.getType(expression));
    }

    @ParameterizedTest
    @CsvSource({
            ">now, NOW",
            "<now, NOW",
            ">=now, NOW",
            "<=now, NOW",
    })
    void getTypeReturnsNowForNowExpressions(final String expression, final String expectedType) {
        assertEquals(ConditionType.valueOf(expectedType), ConditionType.getType(expression));
    }

    @Test
    void getTypeThrowsForUnknownExpression() {
        assertThrows(MatchException.class, () -> ConditionType.getType("unknown"));
    }
}
