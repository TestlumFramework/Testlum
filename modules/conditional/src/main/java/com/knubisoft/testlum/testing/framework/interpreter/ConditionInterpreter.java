package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition o, final CommandResult result) {
        Condition condition = injectCommand(o);
        ScenarioContext scenarioContext = dependencies.getScenarioContext();
        conditionProvider.processCondition(condition.getName(), scenarioContext.getCondition(condition.getSpel()),
                scenarioContext, result);
    }
}
