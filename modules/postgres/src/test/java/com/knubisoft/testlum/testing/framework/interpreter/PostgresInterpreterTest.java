package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.scenario.Postgres;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresInterpreterTest {

    @Mock
    private InterpreterDependencies dependencies;

    @Mock
    private AbstractStorageOperation postgresSqlOperation;

    private PostgresInterpreter interpreter;

    @BeforeEach
    void setUp() throws Exception {
        org.springframework.context.ApplicationContext ctx =
                mock(org.springframework.context.ApplicationContext.class);
        when(dependencies.getContext()).thenReturn(ctx);

        com.knubisoft.testlum.testing.framework.configuration.ConfigProvider configProvider =
                mock(com.knubisoft.testlum.testing.framework.configuration.ConfigProvider.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.framework.configuration.ConfigProvider.class))
                .thenReturn(configProvider);

        com.knubisoft.testlum.testing.framework.util.ConditionProvider conditionProvider =
                mock(com.knubisoft.testlum.testing.framework.util.ConditionProvider.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.framework.util.ConditionProvider.class))
                .thenReturn(conditionProvider);

        com.knubisoft.testlum.testing.framework.FileSearcher fileSearcher =
                mock(com.knubisoft.testlum.testing.framework.FileSearcher.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.framework.FileSearcher.class))
                .thenReturn(fileSearcher);

        com.knubisoft.testlum.testing.framework.util.JacksonService jacksonService =
                mock(com.knubisoft.testlum.testing.framework.util.JacksonService.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.framework.util.JacksonService.class))
                .thenReturn(jacksonService);

        com.knubisoft.testlum.testing.framework.util.StringPrettifier stringPrettifier =
                mock(com.knubisoft.testlum.testing.framework.util.StringPrettifier.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.framework.util.StringPrettifier.class))
                .thenReturn(stringPrettifier);

        com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration globalConfig =
                mock(com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration.class);
        when(ctx.getBean(com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration.class))
                .thenReturn(globalConfig);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        interpreter = new PostgresInterpreter(dependencies);

        Field field = PostgresInterpreter.class.getDeclaredField("postgresSqlOperation");
        field.setAccessible(true);
        field.set(interpreter, postgresSqlOperation);
    }

    @Nested
    class GetOperation {

        @Test
        void returnsPostgresOperation() {
            AbstractStorageOperation result = interpreter.getOperation();
            assertSame(postgresSqlOperation, result);
        }

        @Test
        void returnsNullWhenOperationNotInjected() throws Exception {
            Field field = PostgresInterpreter.class.getDeclaredField("postgresSqlOperation");
            field.setAccessible(true);
            field.set(interpreter, null);

            assertNull(interpreter.getOperation());
        }
    }

    @Nested
    class GetAlias {

        @Test
        void returnsAliasFromCommand() {
            Postgres command = mock(Postgres.class);
            when(command.getAlias()).thenReturn("pg_alias");

            assertEquals("pg_alias", interpreter.getAlias(command));
        }

        @Test
        void returnsNullWhenAliasIsNull() {
            Postgres command = mock(Postgres.class);
            when(command.getAlias()).thenReturn(null);

            assertNull(interpreter.getAlias(command));
        }
    }

    @Nested
    class SetAlias {

        @Test
        void setsAliasOnCommand() {
            Postgres command = mock(Postgres.class);
            interpreter.setAlias(command, "new_alias");

            verify(command).setAlias("new_alias");
        }

        @Test
        void setsNullAlias() {
            Postgres command = mock(Postgres.class);
            interpreter.setAlias(command, null);

            verify(command).setAlias(null);
        }
    }

    @Nested
    class GetQueries {

        @Test
        void returnsQueriesFromCommand() {
            Postgres command = mock(Postgres.class);
            List<String> queries = Arrays.asList(
                    "SELECT * FROM users",
                    "INSERT INTO logs VALUES (1, 'test')"
            );
            when(command.getQuery()).thenReturn(queries);

            List<String> result = interpreter.getQueries(command);

            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyListWhenNoQueries() {
            Postgres command = mock(Postgres.class);
            when(command.getQuery()).thenReturn(Collections.emptyList());

            assertTrue(interpreter.getQueries(command).isEmpty());
        }
    }

    @Nested
    class GetFile {

        @Test
        void returnsFileFromCommand() {
            Postgres command = mock(Postgres.class);
            when(command.getFile()).thenReturn("pg_expected.json");

            assertEquals("pg_expected.json", interpreter.getFile(command));
        }

        @Test
        void returnsNullWhenFileIsNull() {
            Postgres command = mock(Postgres.class);
            when(command.getFile()).thenReturn(null);

            assertNull(interpreter.getFile(command));
        }
    }
}
