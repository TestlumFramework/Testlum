package com.knubisoft.cott.testing.framework.interpreter;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Native;

@InterpreterForClass(Native.class)
public class NativeInterpreter extends AbstractUiInterpreter<Native> {

    public NativeInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Native command, final CommandResult result) {
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.NATIVE));
    }
}
