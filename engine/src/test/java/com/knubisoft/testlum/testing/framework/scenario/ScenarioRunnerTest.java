package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.ui.MobileBrowserDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.NativeDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.WebDriverFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CommandToInterpreterClassMap;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ScenarioRunner} verifying scenario execution,
 * result population, and error handling.
 */
class ScenarioRunnerTest {

    private ScenarioArguments scenarioArguments;
    private ScenarioRunner runner;

    @BeforeEach
    void setUp() {
        final Scenario scenario = createMinimalScenario();
        scenarioArguments = ScenarioArguments.builder()
                .file(new File("test-scenario.xml"))
                .scenario(scenario)
                .browser(null)
                .mobileBrowserDevice(null)
                .nativeDevice(null)
                .containsUiSteps(false)
                .variations(new HashMap<>())
                .build();
        scenarioArguments.setEnvironment("test");

        runner = new ScenarioRunner(scenarioArguments, buildMockContext());
    }

    private Scenario createMinimalScenario() {
        final Scenario scenario = new Scenario();
        final Overview overview = new Overview();
        overview.setName("Test Scenario");
        overview.setDescription("A test");
        scenario.setOverview(overview);
        final Settings settings = new Settings();
        settings.setTags("smoke");
        scenario.setSettings(settings);
        scenario.getCommands().addAll(new ArrayList<>());
        return scenario;
    }

    private org.springframework.context.ApplicationContext buildMockContext() {
        final org.springframework.context.ApplicationContext ctx =
                mock(org.springframework.context.ApplicationContext.class);

        final ResultUtil resultUtil = mock(ResultUtil.class);
        final MobileUtil mobileUtil = mock(MobileUtil.class);
        final BrowserUtil browserUtil = mock(BrowserUtil.class);
        final MobileBrowserDriverFactory mbdf = mock(MobileBrowserDriverFactory.class);
        final WebDriverFactory wdf = mock(WebDriverFactory.class);
        final NativeDriverFactory ndf = mock(NativeDriverFactory.class);
        final ScenarioInjectionUtil injUtil = mock(ScenarioInjectionUtil.class);
        final LogUtil logUtil = mock(LogUtil.class);
        final InterpreterScanner scanner = mock(InterpreterScanner.class);
        final GlobalTestConfiguration config = mock(GlobalTestConfiguration.class);

        when(ctx.getBean(ResultUtil.class)).thenReturn(resultUtil);
        when(ctx.getBean(MobileUtil.class)).thenReturn(mobileUtil);
        when(ctx.getBean(BrowserUtil.class)).thenReturn(browserUtil);
        when(ctx.getBean(MobileBrowserDriverFactory.class)).thenReturn(mbdf);
        when(ctx.getBean(WebDriverFactory.class)).thenReturn(wdf);
        when(ctx.getBean(NativeDriverFactory.class)).thenReturn(ndf);
        when(ctx.getBean(ScenarioInjectionUtil.class)).thenReturn(injUtil);
        when(ctx.getBean(LogUtil.class)).thenReturn(logUtil);
        when(ctx.getBean(InterpreterScanner.class)).thenReturn(scanner);
        when(ctx.getBean(GlobalTestConfiguration.class)).thenReturn(config);

        when(scanner.getInterpreters()).thenReturn(new CommandToInterpreterClassMap());
        when(config.isStopScenarioOnFailure()).thenReturn(false);
        when(browserUtil.getBrowserBy(any(), any())).thenReturn(Optional.empty());
        when(mobileUtil.getMobileBrowserDeviceBy(any(), any())).thenReturn(Optional.empty());
        when(mobileUtil.getNativeDeviceBy(any(), any())).thenReturn(Optional.empty());
        when(injUtil.injectObject(any(Overview.class), any())).thenAnswer(i -> i.getArgument(0));

        return ctx;
    }

    @Nested
    class RunEmptyScenario {
        @Test
        void returnsResultWithOverview() {
            final ScenarioResult result = runner.run();
            assertNotNull(result);
            assertEquals("Test Scenario", result.getName());
        }

        @Test
        void emptyCommandListIsSuccess() {
            final ScenarioResult result = runner.run();
            assertTrue(result.isSuccess());
            assertTrue(result.getCommands().isEmpty());
        }

        @Test
        void setsEnvironment() {
            final ScenarioResult result = runner.run();
            assertEquals("test", result.getEnvironment());
        }

        @Test
        void setsTags() {
            final ScenarioResult result = runner.run();
            assertEquals("smoke", result.getTags());
        }

        @Test
        void setsPath() {
            final ScenarioResult result = runner.run();
            assertEquals("test-scenario.xml", result.getPath());
        }
    }
}
