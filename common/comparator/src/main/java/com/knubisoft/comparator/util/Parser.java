package com.knubisoft.comparator.util;

import com.knubisoft.comparator.alias.Alias;
import lombok.experimental.UtilityClass;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.knubisoft.comparator.constant.CommonConstant;
import com.knubisoft.comparator.constant.RegexpConstant;

@UtilityClass
public class Parser {

    public String parseAllPBracketsToRegexp(final String expression) {
        Set<String> allPBrackets = getAllPBracketsFromExpression(expression);
        return replaceAllPBracketsInExpression(allPBrackets, expression);
    }

    private Set<String> getAllPBracketsFromExpression(final String expression) {
        Matcher matcher = RegexpConstant.P_BRACKETS.matcher(expression);
        Set<String> pBrackets = new HashSet<>();
        while (matcher.find()) {
            pBrackets.add(matcher.group());
        }
        return pBrackets;
    }

    private String replaceAllPBracketsInExpression(final Set<String> pBrackets, final String expression) {
        String result = expression;
        for (String p : pBrackets) {
            String aliasName = p.substring(2, p.length() - 1);
            String regexp = Alias.getPattern(aliasName).pattern();
            result = result.replaceAll(Pattern.quote(p), Matcher.quoteReplacement(regexp));
        }
        return String.format(CommonConstant.ALL_EXPRESSION_TO_REGEXP, result);
    }
}
