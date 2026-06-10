package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Include;

public interface IncludedScenarioRunner {

    void run(Include include,
             InterpreterDependencies dependencies,
             CommandResult result);
}
