package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

abstract class AbstractDatabaseInterpreterTest {

    @TempDir
    File tempDir;

    private ScenarioContext scenarioContext;
    private JacksonService jacksonService;
    private StringPrettifier stringPrettifier;
    private ConditionProvider conditionProvider;
    private AbstractStorageOperation storageOperation;
    private TestDatabaseInterpreter interpreter;

    @BeforeEach
    void setUp() {
        jacksonService = mock(JacksonService.class);
        stringPrettifier = mock(StringPrettifier.class);
        conditionProvider = mock(ConditionProvider.class);
        storageOperation = mock(AbstractStorageOperation.class);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        ConfigProvider configProvider = mock(ConfigProvider.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(configProvider);
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(conditionProvider);

        FileSearcher fileSearcher = mock(FileSearcher.class);
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(fileSearcher);
        when(applicationContext.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(stringPrettifier);

        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        scenarioContext = new ScenarioContext(new HashMap<>());
        File scenarioFile = new File(tempDir, "scenario.xml");

        InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(scenarioFile)
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new TestDatabaseInterpreter(dependencies, storageOperation);
    }

    private void stubCommonMocks(final TestDbCommand command, final StorageOperationResult opResult,
                                  final String alias, final String serialized) {
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
        when(jacksonService.readCopiedValue(anyString(), eq(TestDbCommand.class))).thenReturn(command);
        when(storageOperation.apply(any(ListSource.class), eq(alias))).thenReturn(opResult);
        when(jacksonService.writeValueAsString(any())).thenReturn(serialized);
        when(stringPrettifier.asJsonResult(anyString())).thenAnswer(inv -> (String) inv.getArgument(0));
        when(stringPrettifier.asJsonResult(isNull())).thenReturn("");
        when(stringPrettifier.prettify(anyString())).thenReturn("{}");
    }

    @Nested
    class AcceptImpl {
        @Test
        void executesQueryAndSetsResult() {
            TestDbCommand command = createCommand("test db command", "mydb",
                    List.of("SELECT * FROM users"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult(List.of("row1"));
            stubCommonMocks(command, opResult, "mydb", "[\"row1\"]");

            interpreter.apply(command, result);

            assertEquals("[\"row1\"]", result.getActual());
        }

        @Test
        void setsDefaultAliasWhenNull() {
            CommandResult result = new CommandResult();
            result.setId(1);

            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
            when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");

            TestDbCommand injectedCommand = createCommand("test default alias", null,
                    List.of("SELECT 1"), null);
            when(jacksonService.readCopiedValue(anyString(), eq(TestDbCommand.class))).thenReturn(injectedCommand);

            StorageOperationResult opResult = new StorageOperationResult("ok");
            when(storageOperation.apply(any(ListSource.class), eq("DEFAULT"))).thenReturn(opResult);
            when(jacksonService.writeValueAsString(any())).thenReturn("\"ok\"");
            when(stringPrettifier.asJsonResult(anyString())).thenAnswer(inv -> (String) inv.getArgument(0));
            when(stringPrettifier.asJsonResult(isNull())).thenReturn("");
            when(stringPrettifier.prettify(anyString())).thenReturn("{}");

            interpreter.apply(injectedCommand, result);

            assertEquals("DEFAULT", injectedCommand.getAlias());
        }

        @Test
        void addsDatabaseMetaDataToResult() {
            TestDbCommand command = createCommand("test metadata", "testdb",
                    List.of("INSERT INTO t VALUES(1)", "SELECT * FROM t"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult("data");
            stubCommonMocks(command, opResult, "testdb", "\"data\"");

            interpreter.apply(command, result);

            assertEquals("testdb", result.getMetadata().get("Database alias"));
            assertEquals(List.of("INSERT INTO t VALUES(1)", "SELECT * FROM t"),
                    result.getMetadata().get("Queries"));
        }

        @Test
        void setsContextBodyAfterExecution() {
            TestDbCommand command = createCommand("test context body", "db",
                    List.of("SELECT 1"), "mykey");

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult("body_data");
            stubCommonMocks(command, opResult, "db", "\"body_data\"");

            interpreter.apply(command, result);

            assertEquals("\"body_data\"", scenarioContext.get("mykey"));
        }

        @Test
        void handlesMultipleQueries() {
            TestDbCommand command = createCommand("multiple queries test", "db",
                    List.of("INSERT INTO t VALUES(1)", "UPDATE t SET x=2", "SELECT * FROM t"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult(List.of("result"));
            stubCommonMocks(command, opResult, "db", "[\"result\"]");

            interpreter.apply(command, result);

            @SuppressWarnings("unchecked")
            List<String> queries = (List<String>) result.getMetadata().get("Queries");
            assertEquals(3, queries.size());
        }

        @Test
        void skippedWhenConditionIsFalse() {
            TestDbCommand command = createCommand("skipped command", "db",
                    List.of("SELECT 1"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);

            interpreter.apply(command, result);

            assertTrue(result.getMetadata().isEmpty());
        }

        @Test
        void setsExpectedAndActualOnResult() {
            TestDbCommand command = createCommand("expected test", "db",
                    List.of("SELECT 1"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult("val");
            stubCommonMocks(command, opResult, "db", "\"val\"");

            interpreter.apply(command, result);

            assertNotNull(result.getActual());
            assertNotNull(result.getExpected());
        }
    }

    @Nested
    class StorageOperationInteraction {
        @Test
        void callsApplyWithListSourceAndAlias() {
            TestDbCommand command = createCommand("storage test", "myAlias",
                    List.of("SELECT 1"), null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult("ok");
            stubCommonMocks(command, opResult, "myAlias", "\"ok\"");

            interpreter.apply(command, result);

            verify(storageOperation).apply(any(ListSource.class), eq("myAlias"));
        }

        @Test
        void passesQueriesViaListSource() {
            List<String> queries = List.of("INSERT INTO t1 VALUES(1)", "INSERT INTO t2 VALUES(2)");
            TestDbCommand command = createCommand("list source test", "db", queries, null);

            CommandResult result = new CommandResult();
            result.setId(1);

            StorageOperationResult opResult = new StorageOperationResult("done");
            stubCommonMocks(command, opResult, "db", "\"done\"");

            interpreter.apply(command, result);

            verify(storageOperation).apply(any(ListSource.class), eq("db"));
        }
    }

    private TestDbCommand createCommand(final String comment, final String alias,
                                         final List<String> queries, final String file) {
        TestDbCommand command = new TestDbCommand();
        command.setComment(comment);
        command.setAlias(alias);
        command.setQueries(queries);
        command.setFile(file);
        return command;
    }

    static class TestDbCommand extends AbstractCommand {
        private String alias;
        private List<String> queries;
        private String file;

        public String getAlias() {
            return alias;
        }

        public void setAlias(final String alias) {
            this.alias = alias;
        }

        public List<String> getQueries() {
            return queries;
        }

        public void setQueries(final List<String> queries) {
            this.queries = queries;
        }

        public String getFile() {
            return file;
        }

        public void setFile(final String file) {
            this.file = file;
        }
    }

    static class TestDatabaseInterpreter extends AbstractDatabaseInterpreter<TestDbCommand> {

        private final AbstractStorageOperation operation;

        TestDatabaseInterpreter(final InterpreterDependencies dependencies, final AbstractStorageOperation operation) {
            super(dependencies);
            this.operation = operation;
        }

        @Override
        protected AbstractStorageOperation getOperation() {
            return operation;
        }

        @Override
        protected String getAlias(final TestDbCommand command) {
            return command.getAlias();
        }

        @Override
        protected void setAlias(final TestDbCommand command, final String alias) {
            command.setAlias(alias);
        }

        @Override
        protected List<String> getQueries(final TestDbCommand command) {
            return command.getQueries();
        }

        @Override
        protected String getFile(final TestDbCommand command) {
            return command.getFile();
        }
    }
}
