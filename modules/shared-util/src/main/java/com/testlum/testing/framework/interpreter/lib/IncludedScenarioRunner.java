package com.testlum.testing.framework.interpreter.lib;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Include;

public interface IncludedScenarioRunner {

    void run(Include include,
             InterpreterDependencies dependencies,
             CommandResult result);
}
