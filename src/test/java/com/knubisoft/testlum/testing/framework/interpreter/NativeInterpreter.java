package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Native;

@InterpreterForClass(Native.class)
public class NativeInterpreter extends AbstractUiInterpreter<Native> {

    public NativeInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Native command, final CommandResult result) {
        final ExecutorDependencies executorDependencies = createExecutorDependencies(UiType.NATIVE);
        this.subCommandRunner.runCommands(command.getClickOrInputOrAssert(), result, executorDependencies);
    }
}
