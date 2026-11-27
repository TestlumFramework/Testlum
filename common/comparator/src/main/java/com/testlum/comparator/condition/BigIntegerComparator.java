package com.testlum.comparator.condition;

import com.testlum.comparator.util.ConditionalTypeComparator;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@NoArgsConstructor
public class BigIntegerComparator implements ConditionComparator {

    @Override
    public boolean compare(final String actual, final String expected, final Operator operator) {
        BigInteger actualValue = new BigInteger(actual);
        BigInteger expectedValue = new BigInteger(expected);
        return ConditionalTypeComparator.compareConditions(actualValue, expectedValue, operator);
    }
}
