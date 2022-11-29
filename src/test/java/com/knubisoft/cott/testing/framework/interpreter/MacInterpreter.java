package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Mac;

@InterpreterForClass(Mac.class)
public class MacInterpreter extends AbstractUiInterpreter<Mac> {

    public MacInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mac command, final CommandResult result) {
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.MAC));
    }
}
