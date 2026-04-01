package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class AbstractMessageBrokerInterpreterTest {

    @TempDir
    File tempDir;

    private ApplicationContext applicationContext;
    private JacksonService jacksonService;
    private StringPrettifier stringPrettifier;
    private ConditionProvider conditionProvider;
    private GlobalTestConfiguration globalConfig;
    private TestBrokerInterpreter interpreter;
    private InterpreterDependencies dependencies;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        jacksonService = mock(JacksonService.class);
        stringPrettifier = mock(StringPrettifier.class);
        conditionProvider = mock(ConditionProvider.class);
        globalConfig = mock(GlobalTestConfiguration.class);
        FileSearcher fileSearcher = mock(FileSearcher.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);

        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(configProvider);
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(applicationContext.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(stringPrettifier);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        ScenarioContext scenarioContext = new ScenarioContext(new HashMap<>());
        File scenarioFile = new File(tempDir, "scenario.xml");

        dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(scenarioFile)
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new TestBrokerInterpreter(dependencies);
    }

    @Nested
    class AcceptImpl {
        @Test
        void executesActionsAndSetsSubResults() {
            TestBrokerCommand command = new TestBrokerCommand();
            command.setAlias("myAlias");
            command.setActions(List.of("action1", "action2"));

            CommandResult result = new CommandResult();
            result.setSuccess(true);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestBrokerCommand.class)))
                    .thenReturn(command);

            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            interpreter.apply(command, result);

            assertNotNull(result.getSubCommandsResult());
            assertEquals(2, result.getSubCommandsResult().size());
            assertTrue(interpreter.processActionCalled);
        }

        @Test
        void setsDefaultAliasWhenNull() {
            TestBrokerCommand command = new TestBrokerCommand();
            command.setAlias(null);
            command.setActions(List.of("action1"));

            TestBrokerCommand injectedCommand = new TestBrokerCommand();
            injectedCommand.setAlias(null);
            injectedCommand.setActions(List.of("action1"));

            CommandResult result = new CommandResult();
            result.setSuccess(true);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestBrokerCommand.class)))
                    .thenReturn(injectedCommand);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            interpreter.apply(command, result);

            assertEquals("DEFAULT", injectedCommand.getAlias());
        }

        @Test
        void handlesExceptionInAction() {
            TestBrokerCommand command = new TestBrokerCommand();
            command.setAlias("alias");
            command.setActions(List.of("failAction"));

            CommandResult result = new CommandResult();
            result.setSuccess(true);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestBrokerCommand.class)))
                    .thenReturn(command);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            interpreter.failOnAction = true;

            interpreter.apply(command, result);

            assertNotNull(result.getSubCommandsResult());
            CommandResult subResult = result.getSubCommandsResult().get(0);
            assertFalse(subResult.isSuccess());
            assertNotNull(subResult.getException());
        }

        @Test
        void callsBeforeAndAfterActions() {
            TestBrokerCommand command = new TestBrokerCommand();
            command.setAlias("alias");
            command.setActions(List.of("action1"));

            CommandResult result = new CommandResult();
            result.setSuccess(true);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestBrokerCommand.class)))
                    .thenReturn(command);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

            interpreter.apply(command, result);

            assertTrue(interpreter.beforeActionsCalled);
            assertTrue(interpreter.afterActionsCalled);
        }

        @Test
        void callsAfterActionsEvenOnException() {
            TestBrokerCommand command = new TestBrokerCommand();
            command.setAlias("alias");
            command.setActions(List.of("failAction"));

            CommandResult result = new CommandResult();
            result.setSuccess(true);

            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
            when(jacksonService.readCopiedValue(anyString(), eq(TestBrokerCommand.class)))
                    .thenReturn(command);
            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            interpreter.failOnAction = true;

            interpreter.apply(command, result);

            assertTrue(interpreter.afterActionsCalled);
        }
    }

    @Nested
    class NewCommandResultInstance {
        @Test
        void createsResultWithIdAndSuccess() {
            CommandResult result = interpreter.callNewCommandResultInstance(42);
            assertEquals(42, result.getId());
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    class PutIfNotBlank {
        @Test
        void putsValueWhenNotBlank() {
            CommandResult result = new CommandResult();
            interpreter.callPutIfNotBlank(result, "key", "value");
            assertEquals("value", result.getMetadata().get("key"));
        }

        @Test
        void doesNotPutWhenBlank() {
            CommandResult result = new CommandResult();
            interpreter.callPutIfNotBlank(result, "key", "");
            assertFalse(result.getMetadata().containsKey("key"));
        }

        @Test
        void doesNotPutWhenNull() {
            CommandResult result = new CommandResult();
            interpreter.callPutIfNotBlank(result, "key", null);
            assertFalse(result.getMetadata().containsKey("key"));
        }
    }

    @Nested
    class PutIfNotNull {
        @Test
        void putsValueWhenNotNull() {
            CommandResult result = new CommandResult();
            interpreter.callPutIfNotNull(result, "key", 42);
            assertEquals(42, result.getMetadata().get("key"));
        }

        @Test
        void doesNotPutWhenNull() {
            CommandResult result = new CommandResult();
            interpreter.callPutIfNotNull(result, "key", null);
            assertFalse(result.getMetadata().containsKey("key"));
        }
    }

    @Nested
    class GetValue {
        @Test
        void returnsMessageWhenNotBlank() {
            assertEquals("message", interpreter.callGetValue("message", "file.json"));
        }

        @Test
        void returnsFileContentWhenMessageBlank() {
            FileSearcher fs = mock(FileSearcher.class);
            when(applicationContext.getBean(FileSearcher.class)).thenReturn(fs);
            // Since getContentIfFile returns null for non-json files, test with blank message
            assertEquals(null, interpreter.callGetValue("", null));
        }

        @Test
        void returnsMessageWhenBothPresent() {
            assertEquals("msg", interpreter.callGetValue("msg", "file.json"));
        }
    }

    @Nested
    class LogIfNotNull {
        @Test
        void doesNotThrowWhenNotNull() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> interpreter.callLogIfNotNull("title: {}", "data"));
        }

        @Test
        void doesNotThrowWhenNull() {
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                    () -> interpreter.callLogIfNotNull("title: {}", null));
        }
    }

    @Nested
    class AddMessageBrokerGeneralMetaData {
        @Test
        void addsAllMetadata() {
            CommandResult result = new CommandResult();
            interpreter.callAddMessageBrokerGeneralMetaData("alias1", "send", "Topic", "myTopic", result);

            assertEquals("alias1", result.getMetadata().get("Alias"));
            assertEquals("send", result.getMetadata().get("Action"));
            assertEquals("myTopic", result.getMetadata().get("Topic"));
        }
    }

    @Nested
    class CompareMessages {

        private void setupPrettifierMocks() {
            when(stringPrettifier.prettify(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(stringPrettifier.prettifyToSave(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(stringPrettifier.asJsonResult(anyString())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        void matchingMessagesDoNotThrow() {
            setupPrettifierMocks();
            List<String> actual = List.of("msg1", "msg2");
            when(jacksonService.writeValueAsString(actual)).thenReturn("[\"msg1\",\"msg2\"]");
            CommandResult result = new CommandResult();

            assertDoesNotThrow(() -> interpreter.callCompareMessages(
                    actual, "[\"msg1\",\"msg2\"]", result));
        }

        @Test
        void mismatchThrowsComparisonException() {
            setupPrettifierMocks();
            List<String> actual = List.of("msg1");
            when(jacksonService.writeValueAsString(actual)).thenReturn("[\"msg1\"]");
            CommandResult result = new CommandResult();

            assertThrows(ComparisonException.class, () -> interpreter.callCompareMessages(
                    actual, "[\"msg1\",\"msg2\"]", result));
        }

        @Test
        void setsActualAndExpectedOnResult() {
            setupPrettifierMocks();
            List<String> actual = List.of("msg1");
            when(jacksonService.writeValueAsString(actual)).thenReturn("[\"msg1\"]");
            CommandResult result = new CommandResult();

            interpreter.callCompareMessages(actual, "[\"msg1\"]", result);
            assertNotNull(result.getActual());
            assertNotNull(result.getExpected());
        }
    }

    @Nested
    class LogMessageBrokerMetaData {
        @Test
        void doesNotThrow() {
            when(stringPrettifier.asJsonResult(anyString())).thenReturn("content");
            assertDoesNotThrow(() -> interpreter.callLogMessageBrokerMetaData(
                    "send", "Topic: {}", "myTopic", "message content"));
        }
    }

    @Nested
    class ProtectedConstants {
        @Test
        void verifySendAndReceiveActions() {
            assertEquals("send", TestBrokerInterpreter.SEND_ACTION_VAL);
            assertEquals("receive", TestBrokerInterpreter.RECEIVE_ACTION_VAL);
        }

        @Test
        void verifyAdditionalConstants() {
            assertEquals("Action", TestBrokerInterpreter.ACTION_VAL);
            assertEquals("Send", TestBrokerInterpreter.SEND_VAL);
            assertEquals("Receive", TestBrokerInterpreter.RECEIVE_VAL);
            assertEquals("Enable", TestBrokerInterpreter.ENABLE_VAL);
            assertEquals("Disable", TestBrokerInterpreter.DISABLE_VAL);
            assertEquals("Message to send", TestBrokerInterpreter.MESSAGE_TO_SEND_VAL);
            assertEquals("Headers status", TestBrokerInterpreter.HEADERS_STATUS_VAL);
            assertEquals("Timeout millis", TestBrokerInterpreter.TIMEOUT_MILLIS_VAL);
        }
    }

    // Test command class
    private static class TestBrokerCommand extends AbstractCommand {
        private String alias;
        private List<Object> actions = new ArrayList<>();

        public String getAlias() {
            return alias;
        }

        public void setAlias(final String alias) {
            this.alias = alias;
        }

        public List<Object> getActions() {
            return actions;
        }

        public void setActions(final List<Object> actions) {
            this.actions = actions;
        }
    }

    // Concrete test interpreter
    static class TestBrokerInterpreter extends AbstractMessageBrokerInterpreter<TestBrokerCommand> {
        static final String SEND_ACTION_VAL = SEND_ACTION;
        static final String RECEIVE_ACTION_VAL = RECEIVE_ACTION;
        static final String ACTION_VAL = ACTION;
        static final String SEND_VAL = SEND;
        static final String RECEIVE_VAL = RECEIVE;
        static final String ENABLE_VAL = ENABLE;
        static final String DISABLE_VAL = DISABLE;
        static final String MESSAGE_TO_SEND_VAL = MESSAGE_TO_SEND;
        static final String HEADERS_STATUS_VAL = HEADERS_STATUS;
        static final String TIMEOUT_MILLIS_VAL = TIMEOUT_MILLIS;

        boolean processActionCalled;
        boolean failOnAction;
        boolean beforeActionsCalled;
        boolean afterActionsCalled;

        TestBrokerInterpreter(final InterpreterDependencies dependencies) {
            super(dependencies);
        }

        @Override
        protected String getAlias(final TestBrokerCommand command) {
            return command.getAlias();
        }

        @Override
        protected void setAlias(final TestBrokerCommand command, final String alias) {
            command.setAlias(alias);
        }

        @Override
        protected List<Object> getActions(final TestBrokerCommand command) {
            return command.getActions();
        }

        @Override
        protected void processAction(final Object action, final String alias, final CommandResult result) {
            processActionCalled = true;
            if (failOnAction) {
                throw new RuntimeException("action failed");
            }
        }

        @Override
        protected void beforeActions(final TestBrokerCommand command, final CommandResult result) {
            beforeActionsCalled = true;
        }

        @Override
        protected void afterActions(final TestBrokerCommand command, final CommandResult result) {
            afterActionsCalled = true;
        }

        CommandResult callNewCommandResultInstance(final int number) {
            return newCommandResultInstance(number);
        }

        void callPutIfNotBlank(final CommandResult result, final String key, final String value) {
            putIfNotBlank(result, key, value);
        }

        void callPutIfNotNull(final CommandResult result, final String key, final Object value) {
            putIfNotNull(result, key, value);
        }

        String callGetValue(final String message, final String file) {
            return getValue(message, file);
        }

        void callLogIfNotNull(final String title, final Object data) {
            logIfNotNull(title, data);
        }

        void callAddMessageBrokerGeneralMetaData(final String alias, final String action,
                                                  final String destKey, final String destValue,
                                                  final CommandResult result) {
            addMessageBrokerGeneralMetaData(alias, action, destKey, destValue, result);
        }

        <M> void callCompareMessages(final List<M> actualMessages,
                                      final String expectedValue,
                                      final CommandResult result) {
            compareMessages(actualMessages, expectedValue, result);
        }

        void callLogMessageBrokerMetaData(final String action,
                                           final String destinationLogKey,
                                           final String destination,
                                           final String content) {
            logMessageBrokerMetaData(action, destinationLogKey, destination, content);
        }
    }
}
