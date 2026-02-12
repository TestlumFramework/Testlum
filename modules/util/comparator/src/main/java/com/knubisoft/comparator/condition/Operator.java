package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.exception.MatchException;
import com.knubisoft.comparator.util.LogMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Getter
@RequiredArgsConstructor
public enum Operator {
    MORE_THEN(">", Pattern.compile("^(>)[a-zA-Z0-9-.:/ ]*$")),
    LESS_THEN("<", Pattern.compile("^(<)[<a-zA-Z0-9-:/ ]*$")),
    MORE_THEN_OR_EQUAL(">=", Pattern.compile("^(>=)[a-zA-Z0-9-.:/ ]*$")),
    LESS_THEN_OR_EQUAL("<=", Pattern.compile("^(<=)[a-zA-Z0-9-.:/ ]*$"));

    private final String operatorSign;
    private final Pattern pattern;

    public static Operator getOperatorFromExpression(final String expression) {
        return Arrays.stream(Operator.values())
                .filter(o -> o.getPattern().matcher(expression).matches())
                .findFirst()
                .orElseThrow(() -> new MatchException(format(LogMessage.WRONG_OPERATOR, expression)));
    }

}
