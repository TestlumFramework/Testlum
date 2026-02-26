package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.text.StringEscapeUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
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
    public String inject(String scenarioStepAsString, ScenarioContext scenarioContext) {
        Matcher m = ROUTE_PATTERN.matcher(scenarioStepAsString);
        String formatted = scenarioStepAsString;
        while (m.find()) {
            String csvColumnName = m.group(1);
            String scenarioPlaceholder = m.group(0);
            if (!scenarioPlaceholder.contains("j(")) {
                String valueToReplacePlaceholder = scenarioContext.get(csvColumnName);
                formatted = formatted.replace(scenarioPlaceholder, StringEscapeUtils.escapeJson(valueToReplacePlaceholder));
            }
        }
        return formatted;
    }

}