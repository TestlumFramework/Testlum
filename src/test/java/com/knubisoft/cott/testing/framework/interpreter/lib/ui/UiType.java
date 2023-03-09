package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import org.openqa.selenium.WebDriver;

import java.util.function.Function;

public enum UiType {

    WEB(env -> GlobalTestConfigurationProvider.getWebSettings(env).getBrowserSettings().getTakeScreenshots().isEnable(),
            InterpreterDependencies::getWebDriver),
    NATIVE(env -> GlobalTestConfigurationProvider.getNativeSettings(env).getTakeScreenshots().isEnable(),
            InterpreterDependencies::getNativeDriver),
    MOBILE_BROWSER(env -> GlobalTestConfigurationProvider.getMobilebrowserSettings(env).getTakeScreenshots().isEnable(),
            InterpreterDependencies::getMobilebrowserDriver);

    private final Function<String, Boolean> screenshotFunction;
    private final Function<InterpreterDependencies, WebDriver> driverFunction;

    UiType(final Function<String, Boolean> screenshotFunction,
           final Function<InterpreterDependencies, WebDriver> driverFunction) {
        this.screenshotFunction = screenshotFunction;
        this.driverFunction = driverFunction;
    }

    public boolean isScreenshotsEnabled(final String env) {
        return screenshotFunction.apply(env);
    }

    public WebDriver getAppropriateDriver(final InterpreterDependencies dependencies) {
        return driverFunction.apply(dependencies);
    }
}
