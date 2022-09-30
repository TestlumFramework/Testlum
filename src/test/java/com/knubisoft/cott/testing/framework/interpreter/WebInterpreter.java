package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.Drivers;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.model.global_config.Settings;
import com.knubisoft.cott.testing.model.scenario.Web;
import org.openqa.selenium.WebDriver;

@InterpreterForClass(Web.class)
public class WebInterpreter extends UiInterpreter<Web>{
    public WebInterpreter(InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected WebDriver getDriver(Drivers drivers) {
        return dependencies.getDrivers().getWebDriver();
    }

    @Override
    protected Settings getSettings() {
        return GlobalTestConfigurationProvider.getBrowserSettings();
    }
}
