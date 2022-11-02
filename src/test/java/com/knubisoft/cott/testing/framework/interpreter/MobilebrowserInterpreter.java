package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;
import org.openqa.selenium.WebDriver;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends AbstractUiInterpreter<Mobilebrowser> {

    public MobilebrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mobilebrowser command, final CommandResult result) {
        WebDriver driver = dependencies.getMobilebrowserDriver();
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.MOBILE_BROWSER));
        clearLocalStorage(driver, command.getClearLocalStorageByKey(), result);
        clearCookies(driver, command.isClearCookiesAfterExecution(), result);
    }
}
