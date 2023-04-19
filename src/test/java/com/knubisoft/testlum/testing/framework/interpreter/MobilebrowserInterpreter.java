package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Mobilebrowser;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends AbstractUiInterpreter<Mobilebrowser> {

    public MobilebrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mobilebrowser command, final CommandResult result) {
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.MOBILE_BROWSER));
        clearLocalStorage(dependencies.getMobilebrowserDriver(), command.getClearLocalStorageByKey(), result);
        clearCookies(dependencies.getMobilebrowserDriver(), command.isClearCookiesAfterExecution(), result);
    }
}
