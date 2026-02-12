package com.knubisoft.comparator;

import com.knubisoft.comparator.alias.Alias;
import com.knubisoft.comparator.condition.BigDecimalComparator;
import com.knubisoft.comparator.condition.BigIntegerComparator;
import com.knubisoft.comparator.condition.ConditionType;
import com.knubisoft.comparator.condition.ConditionComparator;
import com.knubisoft.comparator.condition.DateComparator;
import com.knubisoft.comparator.condition.NowComparator;
import com.knubisoft.comparator.condition.Operator;
import com.knubisoft.comparator.constant.CommonConstant;
import com.knubisoft.comparator.util.LogMessage;
import com.knubisoft.comparator.util.Parser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.knubisoft.comparator.ErrorHelper.raise;
import static com.knubisoft.comparator.constant.CommonConstant.EXTRACT_ACTION_THREE;
import static com.knubisoft.comparator.constant.CommonConstant.RIGHT_BRACKET;
import static com.knubisoft.comparator.constant.CommonConstant.T_LEFT_BRACKET;
import static com.knubisoft.comparator.constant.RegexpConstant.ALL_EXPRESSION_IN_C_BRACKETS;
import static com.knubisoft.comparator.constant.RegexpConstant.ALL_EXPRESSION_IN_P_BRACKETS;
import static com.knubisoft.comparator.constant.RegexpConstant.P_BRACKETS;
import static java.lang.String.format;

public class StringComparator extends AbstractObjectComparator<String> {

    public StringComparator(final Mode mode) {
        super(mode);
    }

    //CHECKSTYLE:OFF
    @Override
    public void compare(final String expected, final String actual) {
        if (!Objects.equals(expected, actual)) {
            ComparisonResult result = extractAction(expected);
            if (result instanceof PatternComparison) {
                patternComparison(actual, (PatternComparison) result);
            } else if (result instanceof TreeComparison) {
                treeComparison(actual, (TreeComparison) result);
            } else if (result instanceof ConditionComparison) {
                conditionComparison(actual, (ConditionComparison) result);
            } else {
                raise(format(LogMessage.PROPERTY_NOT_EQUAL, expected, actual));
            }
        }
    }
    //CHECKSTYLE:ON

    private void patternComparison(final String actual, final PatternComparison result) {
        String aliasOrRawPattern = result.getPattern();
        Pattern pattern = Alias.getPattern(aliasOrRawPattern);
        raise(!pattern.matcher(actual).matches(), format(LogMessage.PROPERTY_NOT_MATCH, actual, aliasOrRawPattern));
    }

    private void treeComparison(final String actual, final TreeComparison result) {
        new Comparator(mode).compare(result.getTree(), actual);
    }

    private void conditionComparison(final String actual, final ConditionComparison conditionComparison) {
        Operator operator = Operator.getOperatorFromExpression(conditionComparison.getExpression());
        String expected = conditionComparison.getExpression().replace(operator.getOperatorSign(), CommonConstant.EMPTY);
        boolean result = conditionComparison.getConditionalComparator().compare(actual, expected, operator);
        raise(!result, format(LogMessage.CONDITIONALS_DO_NOT_MATCH, actual, expected));
    }


    private ComparisonResult extractAction(final String v) {
        if (v.length() > EXTRACT_ACTION_THREE) {
            if (v.startsWith(T_LEFT_BRACKET) && v.endsWith(RIGHT_BRACKET)) {
                return new TreeComparison(v.substring(2, v.length() - 1));
            } else {
                return getComparisonResult(v);
            }
        }
        return new Empty();
    }

    private ComparisonResult getComparisonResult(final String expression) {
        if (P_BRACKETS.matcher(expression).find()) {
            return getPatternComparison(expression);
        } else if (ALL_EXPRESSION_IN_C_BRACKETS.matcher(expression).matches()) {
            return getConditionComparison(expression.substring(2, expression.length() - 1));
        } else {
            return new Empty();
        }
    }

    private PatternComparison getPatternComparison(final String expression) {
        if (ALL_EXPRESSION_IN_P_BRACKETS.matcher(expression).matches()) {
            return new PatternComparison(expression.substring(2, expression.length() - 1));
        } else {
            return new PatternComparison(Parser.parseAllPBracketsToRegexp(expression));
        }
    }

    private ConditionComparison getConditionComparison(final String expression) {
        ConditionType type = ConditionType.getType(expression);
        return switch (type) {
            case INTEGER -> new ConditionComparison(expression, new BigIntegerComparator());
            case DECIMAL -> new ConditionComparison(expression, new BigDecimalComparator());
            case DATE -> new ConditionComparison(expression, new DateComparator());
            case NOW -> new ConditionComparison(expression, new NowComparator());
        };
    }

    private interface ComparisonResult {
    }

    @RequiredArgsConstructor
    @Getter
    public static class PatternComparison implements ComparisonResult {
        private final String pattern;
    }

    @RequiredArgsConstructor
    @Getter
    public static class TreeComparison implements ComparisonResult {
        private final String tree;
    }

    @RequiredArgsConstructor
    @Getter
    public static class ConditionComparison implements ComparisonResult {
        private final String expression;
        private final ConditionComparator conditionalComparator;
    }

    public static class Empty implements ComparisonResult {
    }
}
