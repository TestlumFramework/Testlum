package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;

import java.util.List;

public interface SubCommandRunner {

    void runCommands(List<AbstractUiCommand> commandList, CommandResult result, ExecutorDependencies dependencies);

}
