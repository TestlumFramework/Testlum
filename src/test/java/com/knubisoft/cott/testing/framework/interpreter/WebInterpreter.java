package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Web;

@InterpreterForClass(Web.class)
public class WebInterpreter extends AbstractUiInterpreter<Web> {

    public WebInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Web command, final CommandResult result) {
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.WEB));
        clearLocalStorage(dependencies.getWebDriver(), command.getClearLocalStorageByKey(), result);
        clearCookies(dependencies.getWebDriver(), command.isClearCookiesAfterExecution(), result);
    }

}
