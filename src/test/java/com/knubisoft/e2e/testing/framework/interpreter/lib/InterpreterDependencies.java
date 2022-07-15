package com.knubisoft.e2e.testing.framework.interpreter.lib;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.locator.GlobalLocators;
import com.knubisoft.e2e.testing.framework.scenario.ScenarioContext;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.WebElementFinder;
import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
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
    private final FileSearcher fileSearcher;
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
        this.fileSearcher = new FileSearcher(TestResourceSettings.getInstance().getTestResourcesFolder(), file);
    }

    public InterpreterDependencies(final ApplicationContext context,
                                   final File file,
                                   final ScenarioContext scenarioContext,
                                   final AtomicInteger position) {
        this.context = context;
        this.file = file;
        this.scenarioContext = scenarioContext;
        this.position = position;
        this.fileSearcher = new FileSearcher(TestResourceSettings.getInstance().getTestResourcesFolder(), file);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authorization {
        private Map<String, String> headers;
    }
}
