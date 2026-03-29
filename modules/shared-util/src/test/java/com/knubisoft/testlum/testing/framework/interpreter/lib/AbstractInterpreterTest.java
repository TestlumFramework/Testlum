package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Condition;
import com.knubisoft.testlum.testing.model.scenario.FromExpression;
import com.knubisoft.testlum.testing.model.scenario.Var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractInterpreterTest {

    @TempDir
    File tempDir;

    private ApplicationContext applicationContext;
    private ScenarioContext scenarioContext;
    private JacksonService jacksonService;
    private StringPrettifier stringPrettifier;
    private ConditionProvider conditionProvider;
    private FileSearcher fileSearcher;
    private GlobalTestConfiguration globalConfig;
    private TestInterpreter interpreter;
    private InterpreterDependencies dependencies;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        jacksonService = mock(JacksonService.class);
        stringPrettifier = mock(StringPrettifier.class);
        conditionProvider = mock(ConditionProvider.class);
        fileSearcher = mock(FileSearcher.class);
        globalConfig = mock(GlobalTestConfiguration.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);

        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(configProvider);
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(applicationContext.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(stringPrettifier);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        scenarioContext = new ScenarioContext(new HashMap<>());
        File scenarioFile = new File(tempDir, "scenario.xml");

        dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(scenarioFile)
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new TestInterpreter(dependencies);
    }

    @Nested
    class Apply {
        @Test
        void executesAcceptImplWhenConditionIsTrue() {
            TestCommand command = new TestCommand();
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            interpreter.apply(command, result);

            assertTrue(interpreter.acceptImplCalled);
        }

        @Test
        void skipsAcceptImplWhenConditionIsFalse() {
            TestCommand command = new TestCommand();
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);

            interpreter.apply(command, result);

            assertTrue(!interpreter.acceptImplCalled);
        }

        @Test
        void setsCommentWhenPresent() {
            TestCommand command = new TestCommand();
            command.setComment("test comment");
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            interpreter.apply(command, result);

            assertEquals("test comment", result.getComment());
        }

        @Test
        void doesNotSetCommentWhenBlank() {
            TestCommand command = new TestCommand();
            command.setComment("");
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            interpreter.apply(command, result);

            assertNull(result.getComment());
        }

        @Test
        void throwsWhenThresholdExceeded() {
            TestCommand command = new TestCommand();
            command.setThreshold(0);
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            interpreter.sleepDuringAccept = true;

            assertThrows(DefaultFrameworkException.class, () -> interpreter.apply(command, result));
        }

        @Test
        void doesNotThrowWhenThresholdNotExceeded() {
            TestCommand command = new TestCommand();
            command.setThreshold(10000);
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            assertDoesNotThrow(() -> interpreter.apply(command, result));
        }

        @Test
        void doesNotThrowWhenThresholdIsNull() {
            TestCommand command = new TestCommand();
            CommandResult result = new CommandResult();
            result.setId(1);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            assertDoesNotThrow(() -> interpreter.apply(command, result));
        }

        @Test
        void logsWithPositionWhenIdNonZero() {
            TestCommand command = new TestCommand();
            CommandResult result = new CommandResult();
            result.setId(5);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);

            assertDoesNotThrow(() -> interpreter.apply(command, result));
        }

        @Test
        void logsWithoutPositionWhenIdZero() {
            TestCommand command = new TestCommand();
            CommandResult result = new CommandResult();
            result.setId(0);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);

            assertDoesNotThrow(() -> interpreter.apply(command, result));
        }
    }

    @Nested
    class Save {
        @Test
        void savesFileSuccessfully() throws IOException {
            when(stringPrettifier.prettifyToSave(anyString())).thenAnswer(inv -> inv.getArgument(0));
            dependencies.getPosition().set(1);

            interpreter.save("content");

            File saved = new File(tempDir, "action_1_actual.json");
            assertTrue(saved.exists());
            assertEquals("content",
                    org.apache.commons.io.FileUtils.readFileToString(saved, StandardCharsets.UTF_8));
        }
    }

    @Nested
    class ToStringMethod {
        @Test
        void delegatesToJacksonService() {
            Object obj = new Object();
            when(jacksonService.writeValueAsString(obj)).thenReturn("{\"test\":true}");

            assertEquals("{\"test\":true}", interpreter.toString(obj));
        }
    }

    @Nested
    class Inject {
        @Test
        void delegatesToScenarioContext() {
            scenarioContext.set("name", "value");
            assertEquals("value", interpreter.inject("{{name}}"));
        }

        @Test
        void returnsOriginalWithoutPlaceholders() {
            assertEquals("plain text", interpreter.inject("plain text"));
        }

        @Test
        void returnsNullForNull() {
            assertNull(interpreter.inject(null));
        }
    }

    @Nested
    class GetContentIfFile {
        @Test
        void returnsFileContentForJsonFile() {
            when(fileSearcher.searchFileToString(eq("data.json"), any(File.class)))
                    .thenReturn("{\"key\":\"val\"}");

            String result = interpreter.getContentIfFile("data.json");
            assertEquals("{\"key\":\"val\"}", result);
        }

        @Test
        void returnsOriginalForNonJsonContent() {
            assertEquals("plain content", interpreter.getContentIfFile("plain content"));
        }

        @Test
        void returnsOriginalForBlank() {
            assertNull(interpreter.getContentIfFile(null));
            assertEquals("", interpreter.getContentIfFile(""));
        }
    }

    @Nested
    class GetContextBodyKey {
        @Test
        void returnsValueWhenNotBlank() {
            assertEquals("myKey", interpreter.getContextBodyKey("myKey"));
        }

        @Test
        void returnsUuidWhenBlank() {
            String result = interpreter.getContextBodyKey("");
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }

        @Test
        void returnsUuidWhenNull() {
            String result = interpreter.getContextBodyKey(null);
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }
    }

    @Nested
    class SetContextBody {
        @Test
        void setsValueInScenarioContext() {
            interpreter.callSetContextBody("key", "body");
            assertEquals("body", scenarioContext.get("key"));
        }
    }

    @Nested
    class InjectCommand {
        @Test
        void returnsNullForNullInput() {
            assertNull(interpreter.callInjectCommand(null));
        }

        @Test
        void injectsAndReturnsDeserializedObject() {
            TestCommand cmd = new TestCommand();
            cmd.setComment("test");

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"comment\":\"test\"}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestCommand.class))).thenReturn(cmd);

            TestCommand result = interpreter.callInjectCommand(cmd);
            assertNotNull(result);
            assertEquals("test", result.getComment());
        }
    }

    @Nested
    class InjectVarCommand {
        @Test
        void returnsNullForNullInput() {
            assertNull(interpreter.callInjectVarCommand(null));
        }

        @Test
        void injectsVarWithExpression() {
            Var var = new Var();
            var.setName("testVar");
            FromExpression expression = new FromExpression();
            expression.setValue("someValue");
            var.setExpression(expression);

            Var copiedVar = new Var();
            copiedVar.setName("testVar");
            FromExpression copiedExpr = new FromExpression();
            copiedExpr.setValue("someValue");
            copiedVar.setExpression(copiedExpr);

            when(jacksonService.deepCopy(any(Var.class), eq(Var.class))).thenReturn(copiedVar);
            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"name\":\"testVar\"}");
            when(jacksonService.readCopiedValue(anyString(), eq(Var.class))).thenReturn(copiedVar);

            Var result = interpreter.callInjectVarCommand(var);
            assertNotNull(result);
            assertEquals("testVar", result.getName());
        }

        @Test
        void injectsVarWithoutExpression() {
            Var var = new Var();
            var.setName("testVar");

            Var copiedVar = new Var();
            copiedVar.setName("testVar");

            when(jacksonService.deepCopy(any(Var.class), eq(Var.class))).thenReturn(copiedVar);
            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"name\":\"testVar\"}");
            when(jacksonService.readCopiedValue(anyString(), eq(Var.class))).thenReturn(copiedVar);

            Var result = interpreter.callInjectVarCommand(var);
            assertNotNull(result);
        }
    }

    @Nested
    class InjectConditionCommand {
        @Test
        void returnsNullForNullInput() {
            assertNull(interpreter.callInjectConditionCommand(null));
        }

        @Test
        void injectsCondition() {
            Condition condition = new Condition();
            condition.setName("testCond");
            condition.setSpel("'value' == 'value'");

            Condition copiedCondition = new Condition();
            copiedCondition.setName("testCond");
            copiedCondition.setSpel("'value' == 'value'");

            when(jacksonService.deepCopy(any(Condition.class), eq(Condition.class)))
                    .thenReturn(copiedCondition);
            when(jacksonService.writeValueToCopiedString(any()))
                    .thenReturn("{\"name\":\"testCond\"}");
            when(jacksonService.readCopiedValue(anyString(), eq(Condition.class)))
                    .thenReturn(copiedCondition);

            Condition result = interpreter.callInjectConditionCommand(condition);
            assertNotNull(result);
            assertEquals("testCond", result.getName());
        }
    }

    @Nested
    class CheckIfStopScenarioOnFailure {
        @Test
        void doesNotThrowWhenDisabled() {
            assertDoesNotThrow(
                    () -> interpreter.callCheckIfStopScenarioOnFailure(new RuntimeException("err")));
        }

        @Test
        void throwsWhenEnabled() {
            when(globalConfig.isStopScenarioOnFailure()).thenReturn(true);
            TestInterpreter strictInterpreter = new TestInterpreter(dependencies);

            assertThrows(DefaultFrameworkException.class,
                    () -> strictInterpreter.callCheckIfStopScenarioOnFailure(new RuntimeException("err")));
        }
    }

    @Nested
    class EnsureAlias {
        @Test
        void setsDefaultWhenNull() {
            String[] holder = {null};
            interpreter.callEnsureAlias(() -> holder[0], val -> holder[0] = val);
            assertEquals("DEFAULT", holder[0]);
        }

        @Test
        void doesNotOverrideExistingAlias() {
            String[] holder = {"existing"};
            interpreter.callEnsureAlias(() -> holder[0], val -> holder[0] = val);
            assertEquals("existing", holder[0]);
        }
    }

    @Nested
    class NewCompare {
        @Test
        void createsCompareBuilder() {
            CompareBuilder compare = interpreter.newCompare();
            assertNotNull(compare);
        }
    }

    @Nested
    class LogException {
        @Test
        void handlesExceptionWithMessage() {
            assertDoesNotThrow(() -> interpreter.callLogException(new RuntimeException("test error")));
        }

        @Test
        void handlesExceptionWithNullMessage() {
            assertDoesNotThrow(() -> interpreter.callLogException(new RuntimeException((String) null)));
        }

        @Test
        void handlesExceptionWithBlankMessage() {
            assertDoesNotThrow(() -> interpreter.callLogException(new RuntimeException("")));
        }
    }

    @Nested
    class SetExceptionResultDelegation {
        @Test
        void delegatesToCommandResultHelper() {
            CommandResult result = new CommandResult();
            result.setSuccess(true);
            Exception ex = new RuntimeException("err");

            interpreter.callSetExceptionResult(result, ex);

            assertEquals(false, result.isSuccess());
            assertEquals(ex, result.getException());
        }
    }

    @Nested
    class AddHeadersMetaDataDelegation {
        @Test
        void delegatesToCommandResultHelper() {
            CommandResult result = new CommandResult();
            java.util.Map<String, String> headers = java.util.Map.of("Content-Type", "application/json");

            interpreter.callAddHeadersMetaData(headers, result);

            assertNotNull(result.getMetadata().get("Additional headers"));
        }
    }

    private static class TestCommand extends AbstractCommand {
    }

    private static class TestInterpreter extends AbstractInterpreter<TestCommand> {
        boolean acceptImplCalled;
        boolean sleepDuringAccept;

        TestInterpreter(final InterpreterDependencies dependencies) {
            super(dependencies);
        }

        @Override
        protected void acceptImpl(final TestCommand o, final CommandResult result) {
            acceptImplCalled = true;
            if (sleepDuringAccept) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        <Y> Y callInjectCommand(final Y o) {
            return injectCommand(o);
        }

        Var callInjectVarCommand(final Var var) {
            return injectVarCommand(var);
        }

        Condition callInjectConditionCommand(final Condition condition) {
            return injectConditionCommand(condition);
        }

        void callCheckIfStopScenarioOnFailure(final Exception e) {
            checkIfStopScenarioOnFailure(e);
        }

        void callEnsureAlias(final java.util.function.Supplier<String> getAlias,
                             final java.util.function.Consumer<String> setAlias) {
            ensureAlias(getAlias, setAlias);
        }

        void callSetContextBody(final String key, final String body) {
            setContextBody(key, body);
        }

        void callLogException(final Exception ex) {
            logException(ex);
        }

        void callSetExceptionResult(final CommandResult result, final Exception exception) {
            setExceptionResult(result, exception);
        }

        void callAddHeadersMetaData(final java.util.Map<String, String> headers, final CommandResult result) {
            addHeadersMetaData(headers, result);
        }
    }
}
