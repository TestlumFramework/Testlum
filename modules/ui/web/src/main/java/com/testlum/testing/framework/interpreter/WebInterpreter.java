package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Web;

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
