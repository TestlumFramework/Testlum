package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Web;

@InterpreterForClass(Web.class)
public class WebInterpreter extends AbstractInterpreter<Web> {
    public WebInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Web command, final CommandResult result) {
        boolean takeScreenshots = GlobalTestConfigurationProvider.getNativeSettings().getDeviceSettings()
                .getTakeScreenshots().isEnable();
        ExecutorDependencies executorDependencies = new ExecutorDependencies(dependencies.getWebDriver(),
                dependencies.getFile(), dependencies.getScenarioContext(), dependencies.getPosition(), takeScreenshots);
        UiUtil.runCommands(command.getClickOrInputOrAssert(), result, executorDependencies);
        UiUtil.clearLocalStorage(dependencies.getNativeDriver(), command.getClearLocalStorageByKey(), result);
        UiUtil.clearCookies(dependencies.getNativeDriver(), command.isClearCookiesAfterExecution(), result);
    }


}
