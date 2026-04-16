package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
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

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UiExecutorTest {

    @Mock
    private ApplicationContext context;
    @Mock
    private WebDriver driver;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private ImageComparisonUtil imageComparisonUtil;
    @Mock
    private ConditionUtil conditionUtil;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ScenarioInjectionUtil scenarioInjectionUtil;
    @Mock
    private JacksonService jacksonService;
    @Mock
    private StringPrettifier stringPrettifier;

    private TestableUiExecutor executor;
    private File testFile;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        testFile = new File("test-scenario.xml");
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .file(testFile)
                .build();
        executor = new TestableUiExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "conditionUtil", conditionUtil);
        ReflectionTestUtils.setField(executor, "configUtil", configUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "scenarioInjectionUtil", scenarioInjectionUtil);
        ReflectionTestUtils.setField(executor, "jacksonService", jacksonService);
        ReflectionTestUtils.setField(executor, "stringPrettifier", stringPrettifier);
    }

    @Nested
    class Apply {

        @Test
        void setsCommentAndLogsCommand() {
            Click click = new Click();
            click.setComment("test comment");
            CommandResult result = new CommandResult();
            when(jacksonService.deepCopy(eq(click), eq(Click.class))).thenReturn(click);
            when(scenarioInjectionUtil.injectObject(eq(click), eq(scenarioContext))).thenReturn(click);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);

            executor.apply(click, result);

            assertEquals("test comment", result.getComment());
            verify(logUtil).logUICommand(eq(result.getId()), eq(click));
        }

        @Test
        void executesWhenConditionIsTrue() {
            Click click = new Click();
            CommandResult result = new CommandResult();
            when(jacksonService.deepCopy(eq(click), eq(Click.class))).thenReturn(click);
            when(scenarioInjectionUtil.injectObject(eq(click), eq(scenarioContext))).thenReturn(click);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);

            executor.apply(click, result);

            assertTrue(executor.wasExecuted());
        }

        @Test
        void skipsExecutionWhenConditionIsFalse() {
            Click click = new Click();
            CommandResult result = new CommandResult();
            when(jacksonService.deepCopy(eq(click), eq(Click.class))).thenReturn(click);
            when(scenarioInjectionUtil.injectObject(eq(click), eq(scenarioContext))).thenReturn(click);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(false);

            executor.apply(click, result);

            assertFalse(executor.wasExecuted());
        }

        @Test
        void skipsDeepCopyForWebRepeat() {
            WebRepeat webRepeat = new WebRepeat();
            webRepeat.setComment("repeat comment");
            CommandResult result = new CommandResult();
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);

            executor.applyRepeat(webRepeat, result);

            assertEquals("repeat comment", result.getComment());
            verifyNoInteractions(jacksonService);
        }
    }

    @Nested
    class GetContentIfFile {

        @Test
        void returnsFileContentWhenNotBlank() {
            when(fileSearcher.searchFileToString("script.js", testFile)).thenReturn("console.log('hello');");
            when(scenarioContext.inject("console.log('hello');")).thenReturn("console.log('hello');");

            String content = executor.callGetContentIfFile("script.js");

            assertEquals("console.log('hello');", content);
        }

        @Test
        void returnsNullWhenInputIsNull() {
            String content = executor.callGetContentIfFile(null);

            assertNull(content);
            verifyNoInteractions(fileSearcher);
        }

        @Test
        void returnsBlankWhenInputIsBlank() {
            String content = executor.callGetContentIfFile("   ");

            assertEquals("   ", content);
            verifyNoInteractions(fileSearcher);
        }
    }

    @Nested
    class Inject {

        @Test
        void delegatesToScenarioContext() {
            when(scenarioContext.inject("{{var1}}")).thenReturn("value1");

            String result = executor.callInject("{{var1}}");

            assertEquals("value1", result);
        }
    }

    /**
     * Concrete subclass to test the abstract class behavior.
     */
    @SuppressWarnings("unchecked")
    private static class TestableUiExecutor extends AbstractUiExecutor<AbstractUiCommand> {
        private boolean executed;

        TestableUiExecutor(final ExecutorDependencies dependencies) {
            super(dependencies);
        }

        @Override
        protected void execute(final AbstractUiCommand o, final CommandResult result) {
            executed = true;
        }

        boolean wasExecuted() {
            return executed;
        }

        @SuppressWarnings("unchecked")
        void applyRepeat(final WebRepeat repeat, final CommandResult result) {
            apply((AbstractUiCommand) repeat, result);
        }

        String callInject(final String original) {
            return inject(original);
        }

        String callGetContentIfFile(final String fileOrContent) {
            return getContentIfFile(fileOrContent);
        }
    }
}
