package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.util.ConditionalTypeComparator;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;

@NoArgsConstructor
public class NowComparator implements ConditionComparator {

    @SneakyThrows
    @Override
    public boolean compare(final String actual, final String expected, final Operator operator) {
        SimpleDateFormat formatter = DateFormat.getFormatter(actual);
        return ConditionalTypeComparator.compareConditions(formatter.parse(actual), new Date(), operator);
    }
}
