package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.model.scenario.Oracle;
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
class OracleInterpreterTest {

    @Mock
    private InterpreterDependencies dependencies;

    @Mock
    private AbstractStorageOperation oracleOperation;

    private OracleInterpreter interpreter;

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

        interpreter = new OracleInterpreter(dependencies);

        Field field = OracleInterpreter.class.getDeclaredField("oracleOperation");
        field.setAccessible(true);
        field.set(interpreter, oracleOperation);
    }

    @Nested
    class GetOperation {

        @Test
        void returnsOracleOperation() {
            AbstractStorageOperation result = interpreter.getOperation();
            assertSame(oracleOperation, result);
        }

        @Test
        void returnsNullWhenOperationNotInjected() throws Exception {
            Field field = OracleInterpreter.class.getDeclaredField("oracleOperation");
            field.setAccessible(true);
            field.set(interpreter, null);

            assertNull(interpreter.getOperation());
        }
    }

    @Nested
    class GetAlias {

        @Test
        void returnsAliasFromCommand() {
            Oracle command = mock(Oracle.class);
            when(command.getAlias()).thenReturn("oracle_alias");

            assertEquals("oracle_alias", interpreter.getAlias(command));
        }

        @Test
        void returnsNullWhenAliasIsNull() {
            Oracle command = mock(Oracle.class);
            when(command.getAlias()).thenReturn(null);

            assertNull(interpreter.getAlias(command));
        }
    }

    @Nested
    class SetAlias {

        @Test
        void setsAliasOnCommand() {
            Oracle command = mock(Oracle.class);
            interpreter.setAlias(command, "new_alias");

            verify(command).setAlias("new_alias");
        }

        @Test
        void setsNullAlias() {
            Oracle command = mock(Oracle.class);
            interpreter.setAlias(command, null);

            verify(command).setAlias(null);
        }
    }

    @Nested
    class GetQueries {

        @Test
        void returnsQueriesFromCommand() {
            Oracle command = mock(Oracle.class);
            List<String> queries = Arrays.asList("SELECT 1 FROM DUAL", "SELECT SYSDATE FROM DUAL");
            when(command.getQuery()).thenReturn(queries);

            List<String> result = interpreter.getQueries(command);

            assertEquals(2, result.size());
            assertEquals("SELECT 1 FROM DUAL", result.get(0));
        }

        @Test
        void returnsEmptyListWhenNoQueries() {
            Oracle command = mock(Oracle.class);
            when(command.getQuery()).thenReturn(Collections.emptyList());

            assertTrue(interpreter.getQueries(command).isEmpty());
        }
    }

    @Nested
    class GetFile {

        @Test
        void returnsFileFromCommand() {
            Oracle command = mock(Oracle.class);
            when(command.getFile()).thenReturn("oracle_expected.json");

            assertEquals("oracle_expected.json", interpreter.getFile(command));
        }

        @Test
        void returnsNullWhenFileIsNull() {
            Oracle command = mock(Oracle.class);
            when(command.getFile()).thenReturn(null);

            assertNull(interpreter.getFile(command));
        }
    }
}
