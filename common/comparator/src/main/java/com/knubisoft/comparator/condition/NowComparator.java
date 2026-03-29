package com.knubisoft.comparator.condition;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NowComparator implements ConditionComparator {

    @Override
    public boolean compare(final String actual, final String expected, final Operator operator) {
        return DateFormat.findFormat(actual).compareWithNow(actual, operator);
    }
}
