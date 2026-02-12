package com.knubisoft.comparator.constant;

import com.knubisoft.comparator.condition.Operator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegexpConstant {

    public static final String TEMPLATE_FOR_CONDITIONS = "^%s%s";
    public static final String ANY_DECIMAL = "-?\\d+(\\.\\d+)$";
    public static final String ANY_INT = "-?\\d+$";
    public static final String NOW_FOR_REGEXP = "now$";
    public static final Pattern MORE_THEN_INT = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), ANY_INT));
    public static final Pattern LESS_THEN_INT = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), ANY_INT));
    public static final Pattern MORE_THEN_INT_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), ANY_INT));
    public static final Pattern LESS_THEN_INT_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), ANY_INT));
    public static final Pattern MORE_THEN_DECIMAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), ANY_DECIMAL));
    public static final Pattern LESS_THEN_DECIMAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), ANY_DECIMAL));
    public static final Pattern MORE_THEN_DECIMAL_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), ANY_DECIMAL));
    public static final Pattern LESS_THEN_DECIMAL_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), ANY_DECIMAL));
    public static final Pattern MORE_THEN_DATE_TIME_WITH_DASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_DASH));
    public static final Pattern MORE_THEN_DATE_TIME_WITH_SLASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_SLASH));
    public static final Pattern LESS_THEN_DATE_TIME_WITH_DASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_DASH));
    public static final Pattern LESS_THEN_DATE_TIME_WITH_SLASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_SLASH));
    public static final Pattern MORE_THEN_DATE_TIME_WITH_DASH_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_DASH));
    public static final Pattern MORE_THEN_DATE_TIME_WITH_SLASH_OR_EQUAL =
            Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
                    Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_SLASH));
    public static final Pattern LESS_THEN_DATE_TIME_WITH_DASH_OR_EQUAL =
            Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_DASH));
    public static final Pattern LESS_THEN_DATE_TIME_WITH_SLASH_OR_EQUAL =
            Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_TIME_WITH_SLASH));
    public static final Pattern MORE_THEN_DATE_WITH_DASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), RegexpConstant.DATE_WITH_DASH));
    public static final Pattern MORE_THEN_DATE_WITH_SLASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), RegexpConstant.DATE_WITH_SLASH));
    public static final Pattern LESS_THEN_DATE_WITH_DASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), RegexpConstant.DATE_WITH_DASH));
    public static final Pattern LESS_THEN_DATE_WITH_SLASH = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), RegexpConstant.DATE_WITH_SLASH));
    public static final Pattern MORE_THEN_DATE_WITH_DASH_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_WITH_DASH));
    public static final Pattern MORE_THEN_DATE_WITH_SLASH_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_WITH_SLASH));
    public static final Pattern LESS_THEN_DATE_WITH_DASH_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_WITH_DASH));
    public static final Pattern LESS_THEN_DATE_WITH_SLASH_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.DATE_WITH_SLASH));
    public static final Pattern MORE_THEN_TIME = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN.getOperatorSign(), RegexpConstant.TIME));
    public static final Pattern LESS_THEN_TIME = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), RegexpConstant.TIME));
    public static final Pattern MORE_THEN_TIME_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.TIME));
    public static final Pattern LESS_THEN_TIME_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), RegexpConstant.TIME));
    public static final Pattern MORE_THEN_NOW = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
        Operator.MORE_THEN.getOperatorSign(), NOW_FOR_REGEXP));
    public static final Pattern LESS_THEN_NOW = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN.getOperatorSign(), NOW_FOR_REGEXP));
    public static final Pattern MORE_THEN_NOW_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.MORE_THEN_OR_EQUAL.getOperatorSign(), NOW_FOR_REGEXP));
    public static final Pattern LESS_THEN_NOW_OR_EQUAL = Pattern.compile(format(TEMPLATE_FOR_CONDITIONS,
            Operator.LESS_THEN_OR_EQUAL.getOperatorSign(), NOW_FOR_REGEXP));
    public static final Pattern DATE_TIME_WITH_DASH_PATTERN = Pattern.compile(RegexpConstant.DATE_TIME_WITH_DASH);
    public static final Pattern DATE_TIME_WITH_SLASH_PATTERN = Pattern.compile(RegexpConstant.DATE_TIME_WITH_SLASH);
    public static final Pattern DATE_WITH_DASH_PATTERN = Pattern.compile(RegexpConstant.DATE_WITH_DASH);
    public static final Pattern DATE_WITH_SLASH_PATTERN = Pattern.compile(RegexpConstant.DATE_WITH_SLASH);
    public static final String IP_ADDRESS = "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
            + "|[1-9][0-9]|[0-9]))";
    public static final String GOOD_IRI_CHAR = "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
    public static final String IRI = "[" + GOOD_IRI_CHAR + "]([" + GOOD_IRI_CHAR + "\\-]{0,61}["
            + GOOD_IRI_CHAR + "]){0,1}";
    public static final String GOOD_GTLD_CHAR = "a-zA-Z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";
    public static final String GTLD = "[" + GOOD_GTLD_CHAR + "]{2,63}";
    public static final String DATE_TIME_WITH_DASH = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(?:.\\d)?";
    public static final String DATE_TIME_WITH_SLASH = "\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}(?:.\\d)?";
    public static final String DATE_WITH_DASH = "\\d{4}-\\d{2}-\\d{2}";
    public static final String DATE_WITH_SLASH = "\\d{4}/\\d{2}/\\d{2}";
    public static final String TIME = "\\d{2}:\\d{2}:\\d{2}(?:.\\d)?";
    public static final String HOST_NAME = "(" + IRI + "\\.)+" + GTLD;
    public static final String DOMAIN_NAME = "(" + HOST_NAME + "|" + IP_ADDRESS + ")";
    public static final Pattern ALL_EXPRESSION_IN_P_BRACKETS = Pattern
            .compile("^p\\(((?!p\\()([a-zA-Z0-9-.:\\/><=*+?^\\[\\]$&\\|\\s]+))\\)$");
    public static final Pattern ALL_EXPRESSION_IN_C_BRACKETS = Pattern
            .compile("^c\\(((?!c\\()([a-zA-Z0-9-.:/><=*+?^\\[\\]$&\\|\\s]+))\\)$");
    public static final Pattern P_BRACKETS = Pattern.compile("(p\\((.*?)\\))");
}

