package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Web;

@InterpreterForClass(Web.class)
public class WebInterpreter extends AbstractUiInterpreter<Web> {

    public WebInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Web command, final CommandResult result) {
        final ExecutorDependencies executorDependencies = createExecutorDependencies(UiType.WEB);
        this.subCommandRunner.runCommands(command.getClickOrInputOrAssert(), result, executorDependencies);
        clearLocalStorage(dependencies.getWebDriver(), command.getClearLocalStorageByKey(), result);
        clearCookies(dependencies.getWebDriver(), command.isClearCookiesAfterExecution(), result);
    }

}
