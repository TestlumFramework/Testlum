package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.scenario.Clickhouse;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickhouseInterpreterTest {

    @Mock
    private InterpreterDependencies dependencies;

    @Mock
    private AbstractStorageOperation clickhouseOperation;

    private ClickhouseInterpreter interpreter;

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

        interpreter = new ClickhouseInterpreter(dependencies);

        Field field = ClickhouseInterpreter.class.getDeclaredField("clickhouseOperation");
        field.setAccessible(true);
        field.set(interpreter, clickhouseOperation);
    }

    @Nested
    class GetOperation {

        @Test
        void returnsClickhouseOperation() {
            AbstractStorageOperation result = interpreter.getOperation();
            assertSame(clickhouseOperation, result);
        }

        @Test
        void returnsNullWhenOperationNotInjected() throws Exception {
            Field field = ClickhouseInterpreter.class.getDeclaredField("clickhouseOperation");
            field.setAccessible(true);
            field.set(interpreter, null);

            assertNull(interpreter.getOperation());
        }
    }

    @Nested
    class GetAlias {

        @Test
        void returnsAliasFromCommand() {
            Clickhouse command = mock(Clickhouse.class);
            when(command.getAlias()).thenReturn("ch_alias");

            assertEquals("ch_alias", interpreter.getAlias(command));
        }

        @Test
        void returnsNullWhenAliasIsNull() {
            Clickhouse command = mock(Clickhouse.class);
            when(command.getAlias()).thenReturn(null);

            assertNull(interpreter.getAlias(command));
        }
    }

    @Nested
    class SetAlias {

        @Test
        void setsAliasOnCommand() {
            Clickhouse command = mock(Clickhouse.class);
            interpreter.setAlias(command, "new_alias");

            org.mockito.Mockito.verify(command).setAlias("new_alias");
        }

        @Test
        void setsNullAlias() {
            Clickhouse command = mock(Clickhouse.class);
            interpreter.setAlias(command, null);

            org.mockito.Mockito.verify(command).setAlias(null);
        }
    }

    @Nested
    class GetQueries {

        @Test
        void returnsQueriesFromCommand() {
            Clickhouse command = mock(Clickhouse.class);
            List<String> queries = Arrays.asList("SELECT 1", "SELECT 2");
            when(command.getQuery()).thenReturn(queries);

            List<String> result = interpreter.getQueries(command);

            assertEquals(2, result.size());
            assertEquals("SELECT 1", result.get(0));
        }

        @Test
        void returnsEmptyListWhenNoQueries() {
            Clickhouse command = mock(Clickhouse.class);
            when(command.getQuery()).thenReturn(Collections.emptyList());

            List<String> result = interpreter.getQueries(command);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetFile {

        @Test
        void returnsFileFromCommand() {
            Clickhouse command = mock(Clickhouse.class);
            when(command.getFile()).thenReturn("data.sql");

            assertEquals("data.sql", interpreter.getFile(command));
        }

        @Test
        void returnsNullWhenFileIsNull() {
            Clickhouse command = mock(Clickhouse.class);
            when(command.getFile()).thenReturn(null);

            assertNull(interpreter.getFile(command));
        }
    }
}
