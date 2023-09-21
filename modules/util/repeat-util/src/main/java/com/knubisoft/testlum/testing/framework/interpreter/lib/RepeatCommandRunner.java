package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;

import java.util.List;

public interface RepeatCommandRunner {

    void runCommands(List<AbstractCommand> commandList, InterpreterDependencies dependencies, CommandResult result);
}
