package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.global_config.Settings;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
public enum UiType {

    WEB((env, configurationProvider) -> configurationProvider.getWebSettings(env).getBrowserSettings(),
            InterpreterDependencies::getWebDriver),

    MOBILE_BROWSER((env, configurationProvider) -> configurationProvider.getMobileBrowserSettings(env),
            InterpreterDependencies::getMobilebrowserDriver),

    NATIVE((env, configurationProvider) -> configurationProvider.getNativeSettings(env),
            InterpreterDependencies::getNativeDriver);

    private final BiFunction<String, ConfigProvider, Settings> settingsFunction;
    private final Function<InterpreterDependencies, WebDriver> driverFunction;

    public Settings getSettings(final String env, final ConfigProvider configurationProvider) {
        return settingsFunction.apply(env, configurationProvider);
    }

    public WebDriver getAppropriateDriver(final InterpreterDependencies dependencies) {
        return driverFunction.apply(dependencies);
    }
}
