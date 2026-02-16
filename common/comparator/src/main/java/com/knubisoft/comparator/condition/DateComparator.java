package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.util.ConditionalTypeComparator;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;

@NoArgsConstructor
public class DateComparator implements ConditionComparator {

    @SneakyThrows
    @Override
    public boolean compare(final String actual, final String expected, final Operator operator) {
        SimpleDateFormat formatter = DateFormat.getFormatter(expected);
        return ConditionalTypeComparator.compareConditions(formatter.parse(actual),
                formatter.parse(expected), operator);
    }
}
