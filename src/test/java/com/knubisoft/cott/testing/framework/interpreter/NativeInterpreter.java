package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.Drivers;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.global_config.Settings;
import com.knubisoft.cott.testing.model.scenario.Native;
import com.knubisoft.cott.testing.model.scenario.Ui;
import org.openqa.selenium.WebDriver;

@InterpreterForClass(Native.class)
public class NativeInterpreter extends UiInterpreter<Native>{
    @Override
    protected WebDriver getDriver(Drivers drivers) {
        return dependencies.getDrivers().getNativeDriver();
    }

    @Override
    protected Settings getSettings() {
        return GlobalTestConfigurationProvider.getNativeSettings();
    }

    public NativeInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(Ui o, CommandResult result) {
        super.acceptImpl(o, result);
    }


}
