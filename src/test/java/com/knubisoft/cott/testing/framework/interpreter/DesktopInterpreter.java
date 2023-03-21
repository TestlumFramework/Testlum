package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Desktop;

@InterpreterForClass(Desktop.class)
public class DesktopInterpreter extends AbstractUiInterpreter<Desktop> {

    public DesktopInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Desktop command, final CommandResult result) {
        runCommands(command.getClickOrInputOrWait(), result, createExecutorDependencies(UiType.DESKTOP));
    }
}
