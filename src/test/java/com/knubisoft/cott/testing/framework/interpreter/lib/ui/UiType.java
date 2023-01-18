package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import org.openqa.selenium.WebDriver;

import java.util.function.Function;
import java.util.function.Supplier;

public enum UiType {

    WEB(() -> GlobalTestConfigurationProvider.getWebSettings().getBrowserSettings().getTakeScreenshots().isEnable(),
            InterpreterDependencies::getWebDriver),
    NATIVE(() -> GlobalTestConfigurationProvider.getNativeSettings().getTakeScreenshots().isEnable(),
            InterpreterDependencies::getNativeDriver),
    MOBILE_BROWSER(() -> GlobalTestConfigurationProvider.getMobilebrowserSettings().getTakeScreenshots().isEnable(),
            InterpreterDependencies::getMobilebrowserDriver);

    private final Supplier<Boolean> screenshotFunction;
    private final Function<InterpreterDependencies, WebDriver> driverFunction;

    UiType(final Supplier<Boolean> screenshotFunction,
           final Function<InterpreterDependencies, WebDriver> driverFunction) {
        this.screenshotFunction = screenshotFunction;
        this.driverFunction = driverFunction;
    }

    public boolean isScreenshotsEnabled() {
        return screenshotFunction.get();
    }

    public WebDriver getAppropriateDriver(final InterpreterDependencies interpreterDependencies) {
        return driverFunction.apply(interpreterDependencies);
    }
}
