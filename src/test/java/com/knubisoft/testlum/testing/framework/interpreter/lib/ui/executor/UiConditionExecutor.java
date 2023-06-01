package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionHelper;
import com.knubisoft.testlum.testing.model.scenario.UiCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;

@Slf4j
@ExecutorForClass(UiCondition.class)
public class UiConditionExecutor extends AbstractUiExecutor<UiCondition> {

    @Autowired
    private ConditionHelper conditionHelper;

    public UiConditionExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final UiCondition condition, final CommandResult result) {
        try {
            String injectedSpel = inject(condition.getSpel());
            boolean conditionResult = conditionHelper.getConditionFromSpel(injectedSpel, condition.getName(), result);
            dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition.getName(), condition.getSpel());
            throw e;
        }
    }
}
