package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.exception.MatchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for {@link Operator} enum verifying operator parsing from expressions and operator sign values. */
class OperatorTest {

    @ParameterizedTest
    @CsvSource({
            ">100, MORE_THEN",
            "<50, LESS_THEN",
            ">=200, MORE_THEN_OR_EQUAL",
            "<=10, LESS_THEN_OR_EQUAL",
            ">2024-01-01, MORE_THEN",
            ">=2024-01-01, MORE_THEN_OR_EQUAL",
            "<2024/12/31, LESS_THEN",
            "<=2024/12/31, LESS_THEN_OR_EQUAL"
    })
    void getOperatorFromValidExpression(final String expression, final String expectedOperator) {
        final Operator result = Operator.getOperatorFromExpression(expression);
        assertEquals(Operator.valueOf(expectedOperator), result);
    }

    @Test
    void getOperatorFromInvalidExpressionThrows() {
        assertThrows(MatchException.class, () -> Operator.getOperatorFromExpression("invalid"));
    }

    @Test
    void getOperatorFromEmptyExpressionThrows() {
        assertThrows(MatchException.class, () -> Operator.getOperatorFromExpression(""));
    }

    @Test
    void operatorSignValues() {
        assertEquals(">", Operator.MORE_THEN.getOperatorSign());
        assertEquals("<", Operator.LESS_THEN.getOperatorSign());
        assertEquals(">=", Operator.MORE_THEN_OR_EQUAL.getOperatorSign());
        assertEquals("<=", Operator.LESS_THEN_OR_EQUAL.getOperatorSign());
    }
}
