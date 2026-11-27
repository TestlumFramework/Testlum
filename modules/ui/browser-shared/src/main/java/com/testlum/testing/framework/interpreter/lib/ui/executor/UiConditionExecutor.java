package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.UiCondition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExecutorForClass(UiCondition.class)
public class UiConditionExecutor extends AbstractUiExecutor<UiCondition> {

    public UiConditionExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiCondition condition, final CommandResult result) {
        conditionUtil.processCondition(condition.getName(), condition.getSpel(),
                dependencies.getScenarioContext(), result);
    }
}
