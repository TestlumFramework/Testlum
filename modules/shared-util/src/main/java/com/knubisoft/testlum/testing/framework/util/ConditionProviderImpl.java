package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConditionProviderImpl implements ConditionProvider {

    private final ConditionUtil conditionUtil;

    @Override
    public boolean isTrue(final String condition, final ScenarioContext context, final CommandResult result) {
        return conditionUtil.isTrue(condition, context, result);
    }

    @Override
    public void processCondition(final String condition,
                                 final String expression,
                                 final ScenarioContext scenarioContext,
                                 final CommandResult result) {
        conditionUtil.processCondition(condition, expression, scenarioContext, result);
    }
}
