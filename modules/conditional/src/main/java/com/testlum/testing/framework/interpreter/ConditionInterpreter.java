package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.model.scenario.Condition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Condition.class)
public class ConditionInterpreter extends AbstractInterpreter<Condition> {

    public ConditionInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Condition o, final CommandResult result) {
        Condition condition = injectConditionCommand(o);
        ScenarioContext scenarioContext = dependencies.getScenarioContext();
        conditionProvider.processCondition(condition.getName(), scenarioContext.getCondition(condition.getSpel()),
                scenarioContext, result);
    }
}
