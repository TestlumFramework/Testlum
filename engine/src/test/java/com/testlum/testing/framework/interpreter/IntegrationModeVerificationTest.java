package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.db.AbstractStorageOperation;
import com.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import com.testlum.testing.framework.db.source.ListSource;
import com.testlum.testing.framework.exception.ComparisonException;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.AbstractDatabaseInterpreter;
import com.testlum.testing.framework.interpreter.lib.AbstractMessageBrokerInterpreter;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.AbstractCommand;
import com.testlum.testing.model.scenario.Clickhouse;
import com.testlum.testing.model.scenario.Dynamo;
import com.testlum.testing.model.scenario.ElasticSearchResponse;
import com.testlum.testing.model.scenario.Mode;
import com.testlum.testing.model.scenario.Mongo;
import com.testlum.testing.model.scenario.Mysql;
import com.testlum.testing.model.scenario.Oracle;
import com.testlum.testing.model.scenario.Postgres;
import com.testlum.testing.model.scenario.ReceiveKafkaMessage;
import com.testlum.testing.model.scenario.ReceiveRmqMessage;
import com.testlum.testing.model.scenario.ReceiveSqsMessage;
import com.testlum.testing.model.scenario.Redis;
import com.testlum.testing.model.scenario.S3FileDownload;
import com.testlum.testing.model.scenario.SqlDatabase;
import com.testlum.testing.model.scenario.WebsocketReceive;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.elasticsearch.client.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * End-To-End verification test suite for the 'mode' feature (strict vs lenient, default strict)
 * across all 14 integrations supported by Testlum Core:
 * 1. Databases: ClickHouse, DynamoDB, MongoDB, MySQL, Oracle, PostgreSQL, Redis, SQL Database
 * 2. Message Brokers: Kafka, RabbitMQ, SQS
 * 3. S3 (download)
 * 4. Elasticsearch
 * 5. WebSocket (and STOMP receive)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IntegrationModeVerificationTest {

    private static final String EXPECTED_JSON = "{\"id\":1,\"name\":\"test\"}";
    private static final String ACTUAL_JSON_WITH_EXTRA =
            "{\"id\":1,\"name\":\"test\",\"extraField\":\"unexpected\"}";
    private static final String EXPECTED_LIST_JSON = "[{\"id\":1,\"name\":\"test\"}]";

    @TempDir
    File tempDir;

    private JacksonService jacksonService;
    private StringPrettifier stringPrettifier;
    private FileSearcher fileSearcher;
    private InterpreterDependencies dependencies;
    private AbstractStorageOperation mockStorageOperation;
    private Map<String, Object> actualMapWithExtra;
    private List<Object> actualListWithExtra;

    @BeforeEach
    void setUp() {
        jacksonService = new JacksonService();
        stringPrettifier = new StringPrettifier(jacksonService);
        fileSearcher = mock(FileSearcher.class);
        mockStorageOperation = mock(AbstractStorageOperation.class);

        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        final ConditionProvider conditionProvider = mock(ConditionProvider.class);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(stringPrettifier);

        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        actualMapWithExtra = Map.of("id", 1, "name", "test", "extraField", "unexpected");
        actualListWithExtra = List.of(actualMapWithExtra);

        when(fileSearcher.searchFileToString(eq("expected.json"), any())).thenReturn(EXPECTED_JSON);
        when(mockStorageOperation.apply(any(ListSource.class), any()))
                .thenReturn(new StorageOperationResult(actualMapWithExtra));
    }

    private void injectOperation(final Object interpreter,
                                 final String fieldName,
                                 final AbstractStorageOperation op) throws Exception {
        final Field field = interpreter.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(interpreter, op);
    }

    private <T extends AbstractCommand> void runDbStrict(final AbstractDatabaseInterpreter<T> interp,
                                                         final T cmd) {
        cmd.setComment("strict test");
        assertThrows(ComparisonException.class, () -> interp.apply(cmd, new CommandResult()));
    }

    private <T extends AbstractCommand> void runDbLenient(final AbstractDatabaseInterpreter<T> interp,
                                                          final T cmd) {
        cmd.setComment("lenient test");
        assertDoesNotThrow(() -> interp.apply(cmd, new CommandResult()));
    }

    private <T extends AbstractCommand> void runDbDefault(final AbstractDatabaseInterpreter<T> interp,
                                                          final T cmd) {
        cmd.setComment("default test");
        assertThrows(ComparisonException.class, () -> interp.apply(cmd, new CommandResult()));
    }

    private void invokeMessageBrokerComparison(final AbstractMessageBrokerInterpreter<?> interpreter,
                                              final List<?> actualMessages,
                                              final String expectedValue,
                                              final boolean isStrict) throws Exception {
        final Method method = AbstractMessageBrokerInterpreter.class.getDeclaredMethod(
                "compareMessages", List.class, String.class, boolean.class, CommandResult.class);
        method.setAccessible(true);
        try {
            method.invoke(interpreter, actualMessages, expectedValue, isStrict, new CommandResult());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private void invokeS3Compare(final S3Interpreter interpreter,
                                final S3FileDownload download) throws Exception {
        final Method method = S3Interpreter.class.getDeclaredMethod(
                "compare", String.class, String.class, S3FileDownload.class, String.class, CommandResult.class);
        method.setAccessible(true);
        try {
            method.invoke(interpreter, EXPECTED_JSON, ACTUAL_JSON_WITH_EXTRA, download, "test-key",
                    new CommandResult());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private void invokeElasticsearchCompare(final ElasticsearchInterpreter interpreter,
                                            final ElasticSearchResponse expected,
                                            final Response actual) throws Exception {
        final Method method = ElasticsearchInterpreter.class.getDeclaredMethod(
                "compare", ElasticSearchResponse.class, Response.class, CommandResult.class);
        method.setAccessible(true);
        try {
            method.invoke(interpreter, expected, actual, new CommandResult());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private void invokeWebsocketComparison(final WebsocketInterpreter interpreter,
                                           final List<Object> actualContent,
                                           final String expectedContent,
                                           final boolean isStrict) throws Exception {
        final Method method = WebsocketInterpreter.class.getDeclaredMethod(
                "executeComparison", List.class, String.class, boolean.class);
        method.setAccessible(true);
        try {
            method.invoke(interpreter, actualContent, expectedContent, isStrict);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private Response createMockEsResponse(final String body) {
        final Response response = mock(Response.class);
        final StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(new StringEntity(body, ContentType.APPLICATION_JSON));
        return response;
    }

    // 1. ClickHouse
    @Nested
    class ClickHouseModeVerification {
        private ClickhouseInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new ClickhouseInterpreter(dependencies);
            injectOperation(interpreter, "clickhouseOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Clickhouse cmd = new Clickhouse();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Clickhouse cmd = new Clickhouse();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Clickhouse cmd = new Clickhouse();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 2. DynamoDB
    @Nested
    class DynamoDBModeVerification {
        private DynamoDBInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new DynamoDBInterpreter(dependencies);
            injectOperation(interpreter, "dynamoDBOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Dynamo cmd = new Dynamo();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Dynamo cmd = new Dynamo();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Dynamo cmd = new Dynamo();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 3. MongoDB
    @Nested
    class MongoDBModeVerification {
        private MongoDBInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new MongoDBInterpreter(dependencies);
            injectOperation(interpreter, "mongoOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Mongo cmd = new Mongo();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Mongo cmd = new Mongo();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Mongo cmd = new Mongo();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 4. MySQL
    @Nested
    class MySqlModeVerification {
        private MySqlInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new MySqlInterpreter(dependencies);
            injectOperation(interpreter, "mySqlOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Mysql cmd = new Mysql();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Mysql cmd = new Mysql();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Mysql cmd = new Mysql();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 5. Oracle
    @Nested
    class OracleModeVerification {
        private OracleInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new OracleInterpreter(dependencies);
            injectOperation(interpreter, "oracleOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Oracle cmd = new Oracle();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Oracle cmd = new Oracle();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Oracle cmd = new Oracle();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 6. PostgreSQL
    @Nested
    class PostgresModeVerification {
        private PostgresInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new PostgresInterpreter(dependencies);
            injectOperation(interpreter, "postgresSqlOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Postgres cmd = new Postgres();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Postgres cmd = new Postgres();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Postgres cmd = new Postgres();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 7. Redis
    @Nested
    class RedisModeVerification {
        private RedisInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new RedisInterpreter(dependencies);
            injectOperation(interpreter, "redisOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final Redis cmd = new Redis();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final Redis cmd = new Redis();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final Redis cmd = new Redis();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 8. SQL Database
    @Nested
    class SqlDatabaseModeVerification {
        private SqlDatabaseInterpreter interpreter;

        @BeforeEach
        void init() throws Exception {
            interpreter = new SqlDatabaseInterpreter(dependencies);
            injectOperation(interpreter, "sqlDatabaseOperation", mockStorageOperation);
        }

        @Test
        void strictModeRejectsExtraField() {
            final SqlDatabase cmd = new SqlDatabase();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.STRICT);
            runDbStrict(interpreter, cmd);
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final SqlDatabase cmd = new SqlDatabase();
            cmd.setFile("expected.json");
            cmd.setMode(Mode.LENIENT);
            runDbLenient(interpreter, cmd);
        }

        @Test
        void defaultModeRejectsExtraField() {
            final SqlDatabase cmd = new SqlDatabase();
            cmd.setFile("expected.json");
            cmd.setMode(null);
            runDbDefault(interpreter, cmd);
        }
    }

    // 9. Kafka
    @Nested
    class KafkaModeVerification {
        private KafkaInterpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new KafkaInterpreter(dependencies);
        }

        @Test
        void strictModeRejectsExtraField() {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setMode(Mode.STRICT);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setMode(Mode.LENIENT);
            assertDoesNotThrow(() ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void defaultModeRejectsExtraField() {
            final ReceiveKafkaMessage receive = new ReceiveKafkaMessage();
            receive.setMode(null);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }
    }

    // 10. RabbitMQ
    @Nested
    class RabbitMQModeVerification {
        private RabbitMQInterpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new RabbitMQInterpreter(dependencies);
        }

        @Test
        void strictModeRejectsExtraField() {
            final ReceiveRmqMessage receive = new ReceiveRmqMessage();
            receive.setMode(Mode.STRICT);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final ReceiveRmqMessage receive = new ReceiveRmqMessage();
            receive.setMode(Mode.LENIENT);
            assertDoesNotThrow(() ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void defaultModeRejectsExtraField() {
            final ReceiveRmqMessage receive = new ReceiveRmqMessage();
            receive.setMode(null);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }
    }

    // 11. SQS
    @Nested
    class SQSModeVerification {
        private SQSInterpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new SQSInterpreter(dependencies);
        }

        @Test
        void strictModeRejectsExtraField() {
            final ReceiveSqsMessage receive = new ReceiveSqsMessage();
            receive.setMode(Mode.STRICT);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final ReceiveSqsMessage receive = new ReceiveSqsMessage();
            receive.setMode(Mode.LENIENT);
            assertDoesNotThrow(() ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void defaultModeRejectsExtraField() {
            final ReceiveSqsMessage receive = new ReceiveSqsMessage();
            receive.setMode(null);
            assertThrows(ComparisonException.class, () ->
                    invokeMessageBrokerComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }
    }

    // 12. S3 (download)
    @Nested
    class S3ModeVerification {
        private S3Interpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new S3Interpreter(dependencies);
        }

        @Test
        void strictModeRejectsExtraField() {
            final S3FileDownload download = new S3FileDownload();
            download.setMode(Mode.STRICT);
            download.setValue(EXPECTED_JSON);
            assertThrows(ComparisonException.class, () -> invokeS3Compare(interpreter, download));
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final S3FileDownload download = new S3FileDownload();
            download.setMode(Mode.LENIENT);
            download.setValue(EXPECTED_JSON);
            assertDoesNotThrow(() -> invokeS3Compare(interpreter, download));
        }

        @Test
        void defaultModeRejectsExtraField() {
            final S3FileDownload download = new S3FileDownload();
            download.setMode(null);
            download.setValue(EXPECTED_JSON);
            assertThrows(ComparisonException.class, () -> invokeS3Compare(interpreter, download));
        }
    }

    // 13. Elasticsearch
    @Nested
    class ElasticsearchModeVerification {
        private ElasticsearchInterpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new ElasticsearchInterpreter(dependencies);
        }

        @Test
        void strictModeRejectsExtraField() {
            final ElasticSearchResponse expected = new ElasticSearchResponse();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.STRICT);
            final Response actual = createMockEsResponse(ACTUAL_JSON_WITH_EXTRA);
            assertThrows(DefaultFrameworkException.class, () ->
                    invokeElasticsearchCompare(interpreter, expected, actual));
        }

        @Test
        void lenientModeAcceptsExtraField() {
            final ElasticSearchResponse expected = new ElasticSearchResponse();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(Mode.LENIENT);
            final Response actual = createMockEsResponse(ACTUAL_JSON_WITH_EXTRA);
            assertDoesNotThrow(() -> invokeElasticsearchCompare(interpreter, expected, actual));
        }

        @Test
        void defaultModeRejectsExtraField() {
            final ElasticSearchResponse expected = new ElasticSearchResponse();
            expected.setCode(200);
            expected.setFile("expected.json");
            expected.setMode(null);
            final Response actual = createMockEsResponse(ACTUAL_JSON_WITH_EXTRA);
            assertThrows(DefaultFrameworkException.class, () ->
                    invokeElasticsearchCompare(interpreter, expected, actual));
        }
    }

    // 14. WebSocket (and STOMP receive)
    @Nested
    class WebsocketModeVerification {
        private WebsocketInterpreter interpreter;

        @BeforeEach
        void init() {
            interpreter = new WebsocketInterpreter(dependencies);
        }

        @Test
        void rawWebSocketStrictModeRejectsExtraField() {
            final WebsocketReceive receive = new WebsocketReceive();
            receive.setMode(Mode.STRICT);
            assertThrows(ComparisonException.class, () ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void rawWebSocketLenientModeAcceptsExtraField() {
            final WebsocketReceive receive = new WebsocketReceive();
            receive.setMode(Mode.LENIENT);
            assertDoesNotThrow(() ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void rawWebSocketDefaultModeRejectsExtraField() {
            final WebsocketReceive receive = new WebsocketReceive();
            receive.setMode(null);
            assertThrows(ComparisonException.class, () ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(receive.getMode())));
        }

        @Test
        void stompReceiveStrictModeRejectsExtraField() {
            final WebsocketReceive stompReceive = new WebsocketReceive();
            stompReceive.setTopic("/topic/test");
            stompReceive.setMode(Mode.STRICT);
            assertThrows(ComparisonException.class, () ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(stompReceive.getMode())));
        }

        @Test
        void stompReceiveLenientModeAcceptsExtraField() {
            final WebsocketReceive stompReceive = new WebsocketReceive();
            stompReceive.setTopic("/topic/test");
            stompReceive.setMode(Mode.LENIENT);
            assertDoesNotThrow(() ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(stompReceive.getMode())));
        }

        @Test
        void stompReceiveDefaultModeRejectsExtraField() {
            final WebsocketReceive stompReceive = new WebsocketReceive();
            stompReceive.setTopic("/topic/test");
            stompReceive.setMode(null);
            assertThrows(ComparisonException.class, () ->
                    invokeWebsocketComparison(interpreter, actualListWithExtra, EXPECTED_LIST_JSON,
                            Mode.STRICT.equals(stompReceive.getMode())));
        }
    }
}
