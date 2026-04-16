package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ScenarioInjectionUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.Click;
import com.knubisoft.testlum.testing.model.scenario.WebRepeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebRepeatExecutorTest {

    @Mock
    private SubCommandRunner repeatCommandsRunner;
    @Mock
    private GlobalVariations globalVariations;
    @Mock
    private ScenarioInjectionUtil scenarioInjectionUtil;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private WebRepeatExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new WebRepeatExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "repeatCommandsRunner", repeatCommandsRunner);
        ReflectionTestUtils.setField(executor, "globalVariations", globalVariations);
        ReflectionTestUtils.setField(executor, "scenarioInjectionUtil", scenarioInjectionUtil);
    }

    @Nested
    class SimpleRepeat {

        @Test
        void repeatsCommandsGivenNumberOfTimes() {
            WebRepeat repeat = new WebRepeat();
            repeat.setTimes(3);
            Click click = new Click();
            click.setLocator("btn");
            click.setComment("click button");
            repeat.getClickOrInputOrAssert().add(click);
            CommandResult result = new CommandResult();

            executor.execute(repeat, result);

            verify(repeatCommandsRunner, times(3)).runCommands(any(), any(), any(), any());
            assertNotNull(result.getSubCommandsResult());
        }
    }

    @Nested
    class VariationsRepeat {

        @Test
        void usesVariationsToInjectCommands() {
            WebRepeat repeat = new WebRepeat();
            repeat.setVariations("data.csv");
            Click click = new Click();
            click.setLocator("btn");
            click.setComment("click button");
            repeat.getClickOrInputOrAssert().add(click);
            CommandResult result = new CommandResult();
            Map<String, String> variation = Map.of("key", "value");
            when(globalVariations.getVariations("data.csv")).thenReturn(List.of(variation));
            when(scenarioInjectionUtil.injectObjectVariation(any(), any(), any())).thenReturn(click);

            executor.execute(repeat, result);

            verify(repeatCommandsRunner).runCommands(any(), any(), any(), any());
            assertNotNull(result.getSubCommandsResult());
        }
    }
}
