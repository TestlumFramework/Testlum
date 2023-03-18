package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.Builder;
import lombok.Getter;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Getter
public class ExecutorDependencies {

    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final WebDriver driver;
    private final UiType uiType;
    private final String environment;
}
