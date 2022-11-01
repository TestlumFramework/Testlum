package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Mobilebrowser;

@InterpreterForClass(Mobilebrowser.class)
public class MobilebrowserInterpreter extends AbstractInterpreter<Mobilebrowser> {
    public MobilebrowserInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Mobilebrowser command, final CommandResult result) {
        boolean takeScreenshots = GlobalTestConfigurationProvider.getNativeSettings().getDeviceSettings()
                .getTakeScreenshots().isEnable();
        ExecutorDependencies executorDependencies = new ExecutorDependencies(dependencies.getMobilebrowserDriver(),
                dependencies.getFile(), dependencies.getScenarioContext(), dependencies.getPosition(), takeScreenshots);
        UiUtil.runCommands(command.getClickOrInputOrAssert(), result, executorDependencies);
        UiUtil.clearLocalStorage(dependencies.getNativeDriver(), command.getClearLocalStorageByKey(), result);
        UiUtil.clearCookies(dependencies.getNativeDriver(), command.isClearCookiesAfterExecution(), result);
    }

}
