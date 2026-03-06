package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import lombok.*;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class InterpreterDependencies {

    private final ApplicationContext context;
    private final File file;
    private final ScenarioContext scenarioContext;
    private final AtomicInteger position;
    private final String environment;

    private final WebDriver webDriver;
    private final WebDriver mobilebrowserDriver;
    private final WebDriver nativeDriver;

    private Authorization authorization;

    public <T> T getOptionalBean(final String beanName, final Class<T> clazz, final Supplier<T> defaultValue) {
        return this.context.containsBean(beanName)
                ? context.getBean(beanName, clazz)
                : defaultValue.get();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Authorization {
        private Map<String, String> headers;
    }
}
