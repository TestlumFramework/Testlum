package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class ExecutorDependencies {
    private final GlobalTestConfiguration globalTestConfiguration = GlobalTestConfigurationProvider.provide();
    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final WebDriver driver;

    private final boolean takeScreenshots;

    public ExecutorDependencies(final WebDriver driver,
                                final File file,
                                final ScenarioContext scenarioContext,
                                final AtomicInteger position,
                                final boolean takeScreenshots) {
        this.driver = driver;
        this.file = file;
        this.scenarioContext = scenarioContext;
        this.position = position;
        this.takeScreenshots = takeScreenshots;
    }
}
