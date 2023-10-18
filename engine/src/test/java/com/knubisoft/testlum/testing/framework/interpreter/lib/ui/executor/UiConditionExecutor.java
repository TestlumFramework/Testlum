package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProviderImpl.ConditionUtil;
import com.knubisoft.testlum.testing.model.scenario.UiCondition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExecutorForClass(UiCondition.class)
public class UiConditionExecutor extends AbstractUiExecutor<UiCondition> {

    public UiConditionExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiCondition condition, final CommandResult result) {
        ConditionUtil.processCondition(condition.getName(), condition.getSpel(),
                dependencies.getScenarioContext(), result);
    }
}
