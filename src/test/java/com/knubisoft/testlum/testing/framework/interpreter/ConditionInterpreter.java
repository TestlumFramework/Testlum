package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_CONDITION_LOG;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition o, final CommandResult result) {
        Condition condition = injectCommand(o);
        try {
            boolean conditionResult = ConditionUtil.parseFromSpel(condition.getSpel(), condition.getName(), result);
            dependencies.getScenarioContext().setCondition(condition.getName(), conditionResult);
        } catch (Exception e) {
            log.info(FAILED_CONDITION_LOG, condition.getName(), condition.getSpel());
            throw e;
        }
    }
}
