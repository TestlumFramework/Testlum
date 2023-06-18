package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.model.scenario.UiCondition;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;

@Slf4j
@ExecutorForClass(UiCondition.class)
public class UiConditionExecutor extends AbstractUiExecutor<UiCondition> {

    public UiConditionExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiCondition condition, final CommandResult result) {
        try {
            boolean conditionResult = ConditionUtil.parseFromSpel(condition.getSpel(), condition.getName(), result);
            dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition.getName(), condition.getSpel());
            throw e;
        }
    }
}
