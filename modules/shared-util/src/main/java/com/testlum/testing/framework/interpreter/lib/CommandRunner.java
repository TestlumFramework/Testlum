package com.testlum.testing.framework.interpreter.lib;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.AbstractCommand;

import java.util.List;

public interface CommandRunner {

    void runCommands(List<AbstractCommand> commandList,
                     InterpreterDependencies dependencies,
                     CommandResult result,
                     List<CommandResult> subCommandsResult);
}