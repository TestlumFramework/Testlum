package com.knubisoft.comparator.util;

import com.knubisoft.comparator.condition.Operator;
import com.knubisoft.comparator.exception.ComparisonException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConditionalTypeComparator {

    public <T extends Comparable<T>> boolean compareConditions(final T actual,
                                                               final T expected,
                                                               final Operator operator) {
        return switch (operator) {
            case MORE_THEN -> expected.compareTo(actual) < 0;
            case LESS_THEN -> expected.compareTo(actual) > 0;
            case MORE_THEN_OR_EQUAL -> expected.compareTo(actual) <= 0;
            case LESS_THEN_OR_EQUAL -> expected.compareTo(actual) >= 0;
        };
    }
}
