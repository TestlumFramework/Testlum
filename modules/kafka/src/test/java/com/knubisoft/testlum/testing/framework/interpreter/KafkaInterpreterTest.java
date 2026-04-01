package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeader;
import com.knubisoft.testlum.testing.model.scenario.KafkaHeaders;
import com.knubisoft.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.knubisoft.testlum.testing.model.scenario.SendKafkaMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KafkaInterpreterTest {

    @TempDir
    File tempDir;

    private KafkaInterpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new KafkaInterpreter(dependencies);
    }

    @Nested
    class Validate {

        @Test
        void throwsWhenKafkaProducerIsNull() {
            final DefaultFrameworkException ex = assertThrows(
                    DefaultFrameworkException.class,
                    () -> invokeValidate());
            assertTrue(ex.getMessage().contains("Kafka integration is not configured"));
        }

        @Test
        void doesNotThrowWhenKafkaProducerIsPresent() throws Exception {
            final Field field = KafkaInterpreter.class.getDeclaredField("kafkaProducer");
            field.setAccessible(true);
            field.set(interpreter, new HashMap<>());

            assertDoesNotThrow(() -> invokeValidate());
        }

        private void invokeValidate() throws Exception {
            final Method method = KafkaInterpreter.class.getDeclaredMethod("validate");
            method.setAccessible(true);
            try {
                method.invoke(interpreter);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException rte) {
                    throw rte;
                }
                throw new RuntimeException(e.getCause());
            }
        }
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class AddKafkaSendInfo {

        @Test
        void addsSendMetadataToResult() throws Exception {
            final SendKafkaMessage send = new SendKafkaMessage();
            send.setTopic("test-topic");
            send.setKey("test-key");
            send.setCorrelationId("corr-123");
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaSendInfo", SendKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, send, "myAlias", result);

            assertEquals("Send", result.getCommandKey());
            assertEquals("Send message to Kafka", result.getComment());
            assertEquals("myAlias", result.getMetadata().get("Alias"));
            assertEquals("Send", result.getMetadata().get("Action"));
            assertEquals("test-topic", result.getMetadata().get("Topic"));
            assertEquals("test-key", result.getMetadata().get("Key"));
            assertEquals("corr-123", result.getMetadata().get("correlationId"));
        }

        @Test
        void skipsBlankKeyAndCorrelationId() throws Exception {
            final SendKafkaMessage send = new SendKafkaMessage();
            send.setTopic("topic1");
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaSendInfo", SendKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, send, "alias1", result);

            assertEquals("topic1", result.getMetadata().get("Topic"));
            assertTrue(!result.getMetadata().containsKey("Key"));
            assertTrue(!result.getMetadata().containsKey("correlationId"));
        }

        @Test
        void addsHeadersWhenPresent() throws Exception {
            final SendKafkaMessage send = new SendKafkaMessage();
            send.setTopic("topic1");
            final KafkaHeaders kafkaHeaders = new KafkaHeaders();
            final KafkaHeader header = new KafkaHeader();
            header.setName("X-Custom");
            header.setValue("customValue");
            kafkaHeaders.getHeader().add(header);
            send.setHeaders(kafkaHeaders);
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaSendInfo", SendKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, send, "alias1", result);

            assertTrue(result.getMetadata().containsKey("Additional headers"));
        }
    }

    @Nested
    class AddKafkaReceiveInfo {

        @Test
        void addsReceiveMetadataToResult() throws Exception {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setTopic("receive-topic");
            receive.setTimeoutMillis(5000L);
            receive.setHeaders(true);
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaReceiveInfo", ReceiveKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, receive, "recvAlias", result);

            assertEquals("Receive", result.getCommandKey());
            assertEquals("Receive message from Kafka", result.getComment());
            assertEquals("recvAlias", result.getMetadata().get("Alias"));
            assertEquals("Receive", result.getMetadata().get("Action"));
            assertEquals("receive-topic", result.getMetadata().get("Topic"));
            assertEquals("Enable", result.getMetadata().get("Headers status"));
            assertEquals(5000L, result.getMetadata().get("Timeout millis"));
        }

        @Test
        void setsHeadersStatusToDisableWhenFalse() throws Exception {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setTopic("topic1");
            receive.setHeaders(false);
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaReceiveInfo", ReceiveKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, receive, "alias1", result);

            assertEquals("Disable", result.getMetadata().get("Headers status"));
        }

        @Test
        void usesDefaultTimeoutWhenNotSet() throws Exception {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setTopic("topic1");
            final CommandResult result = new CommandResult();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "addKafkaReceiveInfo", ReceiveKafkaMessage.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, receive, "alias1", result);

            assertEquals(1500L, result.getMetadata().get("Timeout millis"));
        }
    }

    @Nested
    class ProcessKafkaAction {

        @Test
        void throwsForUnknownActionType() throws Exception {
            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "processKafkaAction", Object.class, String.class, CommandResult.class);
            method.setAccessible(true);
            final CommandResult result = new CommandResult();

            try {
                method.invoke(interpreter, "unknownAction", "alias", result);
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertTrue(e.getCause() instanceof DefaultFrameworkException);
                assertTrue(e.getCause().getMessage().contains("Unknown Kafka action"));
            }
        }
    }

    @Nested
    class GetAlias {

        @Test
        void returnsAliasFromCommand() throws Exception {
            final com.knubisoft.testlum.testing.model.scenario.Kafka command =
                    new com.knubisoft.testlum.testing.model.scenario.Kafka();
            command.setAlias("testAlias");

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "getAlias", com.knubisoft.testlum.testing.model.scenario.Kafka.class);
            method.setAccessible(true);
            final String result = (String) method.invoke(interpreter, command);

            assertEquals("testAlias", result);
        }
    }

    @Nested
    class SetAlias {

        @Test
        void setsAliasOnCommand() throws Exception {
            final com.knubisoft.testlum.testing.model.scenario.Kafka command =
                    new com.knubisoft.testlum.testing.model.scenario.Kafka();

            final Method method = KafkaInterpreter.class.getDeclaredMethod(
                    "setAlias", com.knubisoft.testlum.testing.model.scenario.Kafka.class, String.class);
            method.setAccessible(true);
            method.invoke(interpreter, command, "newAlias");

            assertEquals("newAlias", command.getAlias());
        }
    }
}
