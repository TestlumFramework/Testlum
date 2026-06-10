package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.IncludedScenarioRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Include;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

@Slf4j
@InterpreterForClass(Include.class)
public class IncludeInterpreter extends AbstractInterpreter<Include> {

    private static final String INCLUDE_FINISHED_LOG =
            LogFormat.withYellow("------- Include is finished -------");
    private static final String SCENARIO_LOG = LogFormat.table("Scenario");

    private final IncludedScenarioRunner includedScenarioRunner;

    public IncludeInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.includedScenarioRunner = dependencies.getContext().getBean(IncludedScenarioRunner.class);
    }

    @Override
    protected void acceptImpl(final Include include, final CommandResult result) {
        Include injected = injectCommand(include);
        log.info(SCENARIO_LOG, injected.getScenario());
        result.put("Scenario", injected.getScenario());
        result.setSubCommandsResult(new LinkedList<>());
        includedScenarioRunner.run(injected, dependencies, result);
        log.info(INCLUDE_FINISHED_LOG);
    }
}
