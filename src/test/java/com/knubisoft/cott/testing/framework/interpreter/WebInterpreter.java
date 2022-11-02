package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractUiInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Web;
import org.openqa.selenium.WebDriver;

@InterpreterForClass(Web.class)
public class WebInterpreter extends AbstractUiInterpreter<Web> {

    public WebInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Web command, final CommandResult result) {
        boolean takeScreenshots = GlobalTestConfigurationProvider.getBrowserSettings().getBrowserSettings()
                .getTakeScreenshots().isEnable();
        WebDriver driver = dependencies.getWebDriver();
        runCommands(command.getClickOrInputOrAssert(), result, createExecutorDependencies(UiType.WEB));
        clearLocalStorage(driver, command.getClearLocalStorageByKey(), result);
        clearCookies(driver, command.isClearCookiesAfterExecution(), result);
    }

}
