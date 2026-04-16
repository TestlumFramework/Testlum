package com.knubisoft.testlum.testing.framework.variations;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;

import java.util.Map;

public interface VariationsProvider {

    String getValue(String variation, Map<String, String> variationMap);

    String getValue(String variation, Map<String, String> variationMap, ScenarioContext scenarioContext);
}
