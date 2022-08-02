package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import com.knubisoft.cott.testing.framework.util.WebElementFinder;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class InterpreterDependencies {
    private final GlobalTestConfiguration globalTestConfiguration = GlobalTestConfigurationProvider.provide();
    private final ApplicationContext context;
    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private WebDriver webDriver;
    private WebElementFinder webElementFinder;
    private GlobalLocators globalLocators;

    private Authorization authorization;

    public InterpreterDependencies(final WebDriver webDriver,
                                   final ApplicationContext context,
                                   final File file,
                                   final ScenarioContext scenarioContext,
                                   final AtomicInteger position) {
        this.webDriver = webDriver;
        this.context = context;
        this.file = file;
        this.scenarioContext = scenarioContext;
        this.position = position;
        this.webElementFinder = new WebElementFinder();
        this.globalLocators = GlobalLocators.getInstance();
    }

    public InterpreterDependencies(final ApplicationContext context,
                                   final File file,
                                   final ScenarioContext scenarioContext,
                                   final AtomicInteger position) {
        this.context = context;
        this.file = file;
        this.scenarioContext = scenarioContext;
        this.position = position;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authorization {
        private Map<String, String> headers;
    }
}
