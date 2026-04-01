package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.ui.MobileBrowserDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.NativeDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.WebDriverFactory;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CommandToInterpreterClassMap;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import com.knubisoft.testlum.testing.model.scenario.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ScenarioRunner} verifying scenario execution,
 * result population, error handling, command processing, and driver creation.
 */
class ScenarioRunnerTest {

    private ScenarioArguments scenarioArguments;
    private ScenarioRunner runner;
    private ResultUtil resultUtil;
    private LogUtil logUtil;
    private BrowserUtil browserUtil;
    private MobileUtil mobileUtil;
    private ScenarioInjectionUtil injUtil;
    private InterpreterScanner scanner;
    private GlobalTestConfiguration config;
    private ApplicationContext ctx;

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

        ctx = buildMockContext();
        runner = new ScenarioRunner(scenarioArguments, ctx);
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

    private ApplicationContext buildMockContext() {
        ctx = mock(ApplicationContext.class);

        resultUtil = mock(ResultUtil.class);
        mobileUtil = mock(MobileUtil.class);
        browserUtil = mock(BrowserUtil.class);
        final MobileBrowserDriverFactory mbdf = mock(MobileBrowserDriverFactory.class);
        final WebDriverFactory wdf = mock(WebDriverFactory.class);
        final NativeDriverFactory ndf = mock(NativeDriverFactory.class);
        injUtil = mock(ScenarioInjectionUtil.class);
        logUtil = mock(LogUtil.class);
        scanner = mock(InterpreterScanner.class);
        config = mock(GlobalTestConfiguration.class);

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

        @Test
        void setsOverviewObject() {
            final ScenarioResult result = runner.run();
            assertNotNull(result.getOverview());
            assertEquals("Test Scenario", result.getOverview().getName());
        }

        @Test
        void incrementsIdAcrossRunners() {
            final ScenarioResult result1 = runner.run();
            ScenarioRunner runner2 = new ScenarioRunner(scenarioArguments, buildMockContext());
            final ScenarioResult result2 = runner2.run();
            assertTrue(result2.getId() > result1.getId());
        }
    }

    @Nested
    class RunWithCommands {

        @Test
        void unknownCommandSetsExceptionResult() {
            AbstractCommand unknownCmd = mock(AbstractCommand.class);
            scenarioArguments.getScenario().getCommands().add(unknownCmd);

            CommandResult cmdResult = new CommandResult();
            cmdResult.setCommandKey("unknown");
            when(resultUtil.newCommandResultInstance(anyInt(), any())).thenReturn(cmdResult);

            ScenarioRunner cmdRunner = new ScenarioRunner(scenarioArguments, ctx);
            ScenarioResult result = cmdRunner.run();

            assertNotNull(result);
            verify(resultUtil).setExceptionResult(eq(cmdResult), any(DefaultFrameworkException.class));
        }

        @Test
        void multipleUnknownCommandsAllProcessed() {
            AbstractCommand cmd1 = mock(AbstractCommand.class);
            AbstractCommand cmd2 = mock(AbstractCommand.class);
            scenarioArguments.getScenario().getCommands().add(cmd1);
            scenarioArguments.getScenario().getCommands().add(cmd2);

            CommandResult cmdResult1 = new CommandResult();
            cmdResult1.setCommandKey("cmd1");
            CommandResult cmdResult2 = new CommandResult();
            cmdResult2.setCommandKey("cmd2");
            when(resultUtil.newCommandResultInstance(anyInt(), any()))
                    .thenReturn(cmdResult1).thenReturn(cmdResult2);

            ScenarioRunner cmdRunner = new ScenarioRunner(scenarioArguments, ctx);
            ScenarioResult result = cmdRunner.run();

            assertEquals(2, result.getCommands().size());
        }
    }

    @Nested
    class FillReportException {

        @Test
        void setsFailureCauseOnFirstException() throws Exception {
            Method fillReportException = ScenarioRunner.class.getDeclaredMethod("fillReportException", Exception.class);
            fillReportException.setAccessible(true);

            // First, run to populate scenarioResult
            runner.run();

            // Access scenarioResult
            java.lang.reflect.Field scenarioResultField = ScenarioRunner.class.getDeclaredField("scenarioResult");
            scenarioResultField.setAccessible(true);
            ScenarioResult scenarioResult = (ScenarioResult) scenarioResultField.get(runner);

            fillReportException.invoke(runner, new RuntimeException("first error"));

            assertFalse(scenarioResult.isSuccess());
            assertEquals("first error", scenarioResult.getCause());
        }

        @Test
        void doesNotOverwriteCauseOnSubsequentExceptions() throws Exception {
            Method fillReportException = ScenarioRunner.class.getDeclaredMethod("fillReportException", Exception.class);
            fillReportException.setAccessible(true);

            runner.run();

            java.lang.reflect.Field scenarioResultField = ScenarioRunner.class.getDeclaredField("scenarioResult");
            scenarioResultField.setAccessible(true);
            ScenarioResult scenarioResult = (ScenarioResult) scenarioResultField.get(runner);

            fillReportException.invoke(runner, new RuntimeException("first error"));
            fillReportException.invoke(runner, new RuntimeException("second error"));

            assertEquals("first error", scenarioResult.getCause());
        }

        @Test
        void usesClassNameWhenMessageIsBlank() throws Exception {
            Method fillReportException = ScenarioRunner.class.getDeclaredMethod("fillReportException", Exception.class);
            fillReportException.setAccessible(true);

            runner.run();

            java.lang.reflect.Field scenarioResultField = ScenarioRunner.class.getDeclaredField("scenarioResult");
            scenarioResultField.setAccessible(true);
            ScenarioResult scenarioResult = (ScenarioResult) scenarioResultField.get(runner);

            fillReportException.invoke(runner, new RuntimeException((String) null));

            assertEquals("RuntimeException", scenarioResult.getCause());
        }
    }

    @Nested
    class GetInterpreterOrThrow {

        @Test
        void throwsForUnmappedCommand() throws Exception {
            Method getInterpreterOrThrow = ScenarioRunner.class.getDeclaredMethod(
                    "getInterpreterOrThrow", AbstractCommand.class);
            getInterpreterOrThrow.setAccessible(true);

            AbstractCommand unknownCmd = mock(AbstractCommand.class);

            try {
                getInterpreterOrThrow.invoke(runner, unknownCmd);
                fail("Expected exception");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertInstanceOf(DefaultFrameworkException.class, e.getCause());
            }
        }
    }

    @Nested
    class StopScenarioOnFailure {

        @Test
        void stopsExecutionAfterFirstFailureWhenEnabled() {
            when(config.isStopScenarioOnFailure()).thenReturn(true);

            AbstractCommand cmd1 = mock(AbstractCommand.class);
            AbstractCommand cmd2 = mock(AbstractCommand.class);
            scenarioArguments.getScenario().getCommands().add(cmd1);
            scenarioArguments.getScenario().getCommands().add(cmd2);

            CommandResult failResult = new CommandResult();
            failResult.setCommandKey("fail");
            failResult.setException(new RuntimeException("fail"));
            when(resultUtil.newCommandResultInstance(anyInt(), any())).thenReturn(failResult);
            doAnswer(invocation -> {
                CommandResult r = invocation.getArgument(0);
                r.setException(new RuntimeException("fail"));
                return null;
            }).when(resultUtil).setExceptionResult(any(), any());

            ScenarioRunner stopRunner = new ScenarioRunner(scenarioArguments, ctx);
            ScenarioResult result = stopRunner.run();

            // Only one command should be processed before stop
            assertEquals(1, result.getCommands().size());
        }
    }
}
