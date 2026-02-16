package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;

public interface ConditionProvider {

    boolean isTrue(String condition, ScenarioContext context, CommandResult result);

    void processCondition(String condition, String expression, ScenarioContext scenarioContext, CommandResult result);
}
