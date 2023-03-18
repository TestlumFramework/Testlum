package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.scenario.ScenarioContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Builder
@Getter
@Setter
public class InterpreterDependencies {

    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final String environment;

    private final WebDriver webDriver;
    private final WebDriver mobilebrowserDriver;
    private final WebDriver nativeDriver;

    private Authorization authorization;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authorization {
        private Map<String, String> headers;
    }
}
