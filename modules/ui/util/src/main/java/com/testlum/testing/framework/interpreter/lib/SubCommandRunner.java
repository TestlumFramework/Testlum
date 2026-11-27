package com.testlum.testing.framework.interpreter.lib;

import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.AbstractUiCommand;

import java.util.List;

public interface SubCommandRunner {

    void runCommands(List<AbstractUiCommand> commandList,
                     CommandResult result,
                     ExecutorDependencies dependencies);

    void runCommands(List<AbstractUiCommand> commandList,
                     ExecutorDependencies dependencies,
                     CommandResult result,
                     List<CommandResult> subCommandsResult);

}
