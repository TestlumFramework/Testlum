package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    @Override
    public boolean isApplicable(String scenarioStepAsString) {
        Matcher m = ROUTE_PATTERN.matcher(scenarioStepAsString);
        boolean foundRegularVariation = false;
        while (m.find()) {
            String scenarioPlaceholder = m.group(0);
            if (!scenarioPlaceholder.contains("j(")) {
                foundRegularVariation = true;
                break;
            }
        }
        return foundRegularVariation;
    }

    @Override
    public String injectVariationsValues(final String scenarioStepAsString,
                                         final ScenarioContext scenarioContext,
                                         final boolean escapeSpelQuotes) {
        Matcher m = ROUTE_PATTERN.matcher(scenarioStepAsString);
        String formatted = scenarioStepAsString;
        while (m.find()) {
            String firstSubsequence = m.group(1);
            String zeroSubsequence = m.group(0);
            String value = scenarioContext.get(firstSubsequence);
            value = escapeSpelQuotes ? escapeSpelQuotes(value) : StringEscapeUtils.escapeJson(value);
            formatted = formatted.replace(zeroSubsequence, value);
        }
        return formatted;
    }
}