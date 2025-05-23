package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.Builder;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Getter
public class ExecutorDependencies {

    private final ApplicationContext context;
    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final WebDriver driver;
    private final UiType uiType;
    private final String environment;
}
