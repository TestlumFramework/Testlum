package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Mobilebrowser;

@InterpreterForClass(Mobilebrowser.class)
public class MobileBrowserInterpreter extends AbstractUiInterpreter<Mobilebrowser> {

    public MobileBrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mobilebrowser command, final CommandResult result) {
        final ExecutorDependencies executorDependencies = createExecutorDependencies(UiType.MOBILE_BROWSER);
        this.subCommandRunner.runCommands(command.getClickOrInputOrAssert(), result, executorDependencies);
        clearLocalStorage(dependencies.getMobilebrowserDriver(), command.getClearLocalStorageByKey(), result);
        clearCookies(dependencies.getMobilebrowserDriver(), command.isClearCookiesAfterExecution(), result);
    }
}
