package com.testlum.testing.framework.util;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;

public interface ConditionProvider {

    boolean isTrue(String condition, ScenarioContext context, CommandResult result);

    void processCondition(String condition, String expression, ScenarioContext scenarioContext, CommandResult result);
}
