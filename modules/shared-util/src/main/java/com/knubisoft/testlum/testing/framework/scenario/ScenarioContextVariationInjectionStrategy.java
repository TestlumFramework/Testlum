package com.knubisoft.testlum.testing.framework.scenario;

public interface ScenarioContextVariationInjectionStrategy {

    boolean isApplicable(String context);

    String injectVariationsValues(String scenarioStepAsString, ScenarioContext scenarioContext, boolean escapeSpelQuotes);

    default String escapeSpelQuotes(final String value) {
        return value.replaceAll("'", "''");
    }

}
