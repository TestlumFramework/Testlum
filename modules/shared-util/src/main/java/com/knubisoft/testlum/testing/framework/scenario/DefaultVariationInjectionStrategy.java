package com.knubisoft.testlum.testing.framework.scenario;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultVariationInjectionStrategy implements ScenarioContextVariationInjectionStrategy {

    private static final String ROUTE_REGEXP = "\\{\\{(.*?)}}";
    private static final Pattern ROUTE_PATTERN = Pattern.compile(ROUTE_REGEXP, Pattern.DOTALL);

    @Override
    public boolean isApplicable(final String scenarioStepAsString) {
        return ROUTE_PATTERN.matcher(scenarioStepAsString).find();
    }

    @Override
    public String injectVariationsValues(final String scenarioStepAsString,
                                         final ScenarioContext scenarioContext,
                                         final boolean escapeSpelQuotes) {
        if (null == scenarioStepAsString) {
            return null;
        }
        if (StringUtils.isBlank(scenarioStepAsString)) {
            return "";
        }
        Matcher m = ROUTE_PATTERN.matcher(scenarioStepAsString);
        String formatted = scenarioStepAsString;
        while (m.find()) {
            formatted = injectDefaultPlaceholder(scenarioContext, escapeSpelQuotes, m, formatted);
        }
        return formatted;
    }

    private @NotNull String injectDefaultPlaceholder(final ScenarioContext scenarioContext,
                                                     final boolean escapeSpelQuotes,
                                                     final Matcher m,
                                                     final String formatted) {
        String csvColumnName = m.group(1);
        String scenarioPlaceholder = m.group(0);
        String value = scenarioContext.get(csvColumnName);
        value = escapeSpelQuotes ? escapeSpelQuotes(value) : StringEscapeUtils.escapeJson(value);
        return formatted.replace(scenarioPlaceholder, value);
    }
}