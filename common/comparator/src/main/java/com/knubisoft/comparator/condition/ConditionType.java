package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.constant.RegexpConstant;
import com.knubisoft.comparator.exception.MatchException;
import com.knubisoft.comparator.util.LogMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.String.format;

@RequiredArgsConstructor
@Getter
public enum ConditionType {
    INTEGER(Arrays.asList(
            RegexpConstant.MORE_THEN_INT,
            RegexpConstant.LESS_THEN_INT,
            RegexpConstant.MORE_THEN_INT_OR_EQUAL,
            RegexpConstant.LESS_THEN_INT_OR_EQUAL
    )),
    DECIMAL(Arrays.asList(
            RegexpConstant.MORE_THEN_DECIMAL,
            RegexpConstant.LESS_THEN_DECIMAL,
            RegexpConstant.MORE_THEN_DECIMAL_OR_EQUAL,
            RegexpConstant.LESS_THEN_DECIMAL_OR_EQUAL
    )),
    DATE(Arrays.asList(
            RegexpConstant.MORE_THEN_DATE_TIME_WITH_DASH,
            RegexpConstant.MORE_THEN_DATE_TIME_WITH_SLASH,
            RegexpConstant.LESS_THEN_DATE_TIME_WITH_DASH,
            RegexpConstant.LESS_THEN_DATE_TIME_WITH_SLASH,
            RegexpConstant.MORE_THEN_DATE_TIME_WITH_DASH_OR_EQUAL,
            RegexpConstant.MORE_THEN_DATE_TIME_WITH_SLASH_OR_EQUAL,
            RegexpConstant.LESS_THEN_DATE_TIME_WITH_DASH_OR_EQUAL,
            RegexpConstant.LESS_THEN_DATE_TIME_WITH_SLASH_OR_EQUAL,
            RegexpConstant.MORE_THEN_DATE_WITH_DASH,
            RegexpConstant.MORE_THEN_DATE_WITH_SLASH,
            RegexpConstant.LESS_THEN_DATE_WITH_DASH,
            RegexpConstant.LESS_THEN_DATE_WITH_SLASH,
            RegexpConstant.MORE_THEN_DATE_WITH_DASH_OR_EQUAL,
            RegexpConstant.MORE_THEN_DATE_WITH_SLASH_OR_EQUAL,
            RegexpConstant.LESS_THEN_DATE_WITH_DASH_OR_EQUAL,
            RegexpConstant.LESS_THEN_DATE_WITH_SLASH_OR_EQUAL,
            RegexpConstant.MORE_THEN_TIME,
            RegexpConstant.LESS_THEN_TIME,
            RegexpConstant.MORE_THEN_TIME_OR_EQUAL,
            RegexpConstant.LESS_THEN_TIME_OR_EQUAL
    )),
    NOW(Arrays.asList(
            RegexpConstant.MORE_THEN_NOW,
            RegexpConstant.LESS_THEN_NOW,
            RegexpConstant.MORE_THEN_NOW_OR_EQUAL,
            RegexpConstant.LESS_THEN_NOW_OR_EQUAL
    ));

    private final List<Pattern> patterns;

    public static ConditionType getType(final String expression) {
        return Arrays.stream(ConditionType.values())
                .filter(type -> type.getPatterns().stream().anyMatch(pattern -> pattern.matcher(expression).matches()))
                .findFirst()
                .orElseThrow(() -> new MatchException(format(LogMessage.WRONG_OPERATION, expression)));
    }
}
