package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.model.global_config.Settings;
import org.openqa.selenium.WebDriver;

import java.util.function.Function;

import static com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider.getWebSettings;

public enum UiType {

    WEB(env -> getWebSettings(env).getBrowserSettings(),
            InterpreterDependencies::getWebDriver),

    MOBILE_BROWSER(GlobalTestConfigurationProvider::getMobilebrowserSettings,
            InterpreterDependencies::getMobilebrowserDriver),

    NATIVE(GlobalTestConfigurationProvider::getNativeSettings,
            InterpreterDependencies::getNativeDriver),

    DESKTOP(GlobalTestConfigurationProvider::getDesktopSettings,
            InterpreterDependencies::getDesktopDriver);

    private final Function<String, Settings> settingsFunction;
    private final Function<InterpreterDependencies, WebDriver> driverFunction;

    UiType(final Function<String, Settings> settingsFunction,
           final Function<InterpreterDependencies, WebDriver> driverFunction) {
        this.settingsFunction = settingsFunction;
        this.driverFunction = driverFunction;
    }

    public Settings getSettings(final String env) {
        return settingsFunction.apply(env);
    }

    public WebDriver getAppropriateDriver(final InterpreterDependencies dependencies) {
        return driverFunction.apply(dependencies);
    }
}
