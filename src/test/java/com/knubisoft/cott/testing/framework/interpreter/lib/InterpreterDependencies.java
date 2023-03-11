package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
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

    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final WebDriver webDriver;
    private final WebDriver nativeDriver;
    private final WebDriver mobilebrowserDriver;
    private final ApplicationContext cxt;

    private Authorization authorization;

    public InterpreterDependencies(final ApplicationContext ctx,
                                   final WebDriver webDriver,
                                   final WebDriver nativeDriver,
                                   final WebDriver mobilebrowserDriver,
                                   final File file,
                                   final ScenarioContext scenarioContext,
                                   final AtomicInteger position) {
        this.cxt = ctx;
        this.webDriver = webDriver;
        this.nativeDriver = nativeDriver;
        this.mobilebrowserDriver = mobilebrowserDriver;
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
