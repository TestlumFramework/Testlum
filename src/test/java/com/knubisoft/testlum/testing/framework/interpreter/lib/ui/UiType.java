package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.global_config.Settings;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum UiType {

    WEB((env, configurationProvider) -> configurationProvider.getWebSettings(env).getBrowserSettings(),
            InterpreterDependencies::getWebDriver),

    MOBILE_BROWSER((env, configurationProvider) -> configurationProvider.getMobilebrowserSettings(env),
            InterpreterDependencies::getMobilebrowserDriver),

    NATIVE((env, configurationProvider) -> configurationProvider.getNativeSettings(env),
            InterpreterDependencies::getNativeDriver);

    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;
    private final BiFunction<String, GlobalTestConfigurationProvider, Settings> settingsFunction;
    private final Function<InterpreterDependencies, WebDriver> driverFunction;

    UiType(final BiFunction<String, GlobalTestConfigurationProvider, Settings> settingsFunction,
           final Function<InterpreterDependencies, WebDriver> driverFunction) {
        this.settingsFunction = settingsFunction;
        this.driverFunction = driverFunction;
    }

    public Settings getSettings(final String env) {
        return settingsFunction.apply(env, globalTestConfigurationProvider);
    }

    public WebDriver getAppropriateDriver(final InterpreterDependencies dependencies) {
        return driverFunction.apply(dependencies);
    }
}
