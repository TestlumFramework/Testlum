package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class InterpreterDependencies {

    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final WebDriver webDriver;
    private final WebDriver nativeDriver;
    private final WebDriver mobilebrowserDriver;

    private Authorization authorization;

    private final String environment;

    public InterpreterDependencies(final WebDriver webDriver,
                                   final WebDriver nativeDriver,
                                   final WebDriver mobilebrowserDriver,
                                   final String environment,
                                   final File file,
                                   final ScenarioContext scenarioContext,
                                   final AtomicInteger position) {
        this.webDriver = webDriver;
        this.nativeDriver = nativeDriver;
        this.mobilebrowserDriver = mobilebrowserDriver;
        this.environment = environment;
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
