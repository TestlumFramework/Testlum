package com.knubisoft.testlum.testing.framework.scenario;

import java.util.Map;

public interface ScenarioContextVariationInjectionStrategy {

    boolean isApplicable(String context);

    String inject(String scenarioStepAsString, ScenarioContext scenarioContext);
}
