package com.testlum.comparator.condition;

import com.testlum.comparator.util.ConditionalTypeComparator;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class BigDecimalComparator implements ConditionComparator {

    @Override
    public boolean compare(final String actual, final String expected, final Operator operator) {
        BigDecimal actualValue = new BigDecimal(actual);
        BigDecimal expectedValue = new BigDecimal(expected);
        return ConditionalTypeComparator.compareConditions(actualValue, expectedValue, operator);
    }
}
