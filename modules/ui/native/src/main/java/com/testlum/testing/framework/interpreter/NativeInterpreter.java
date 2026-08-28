package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Native;

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
