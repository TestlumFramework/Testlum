package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExecutorProvider} verifying executor lookup
 * and initialization behavior.
 */
@ExtendWith(MockitoExtension.class)
class ExecutorProviderTest {

    @Mock
    private ExecutorScanner executorScanner;

    private ExecutorProvider executorProvider;

    @BeforeEach
    void setUp() {
        executorProvider = new ExecutorProvider(executorScanner);
    }

    @Nested
    class Init {

        @Test
        void initLoadsExecutorMap() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);

            assertDoesNotThrow(() -> executorProvider.init());

            verify(executorScanner).getExecutors();
        }
    }

    @Nested
    class GetAppropriateExecutor {

        @Test
        void throwsWhenExecutorNotFound() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);
            executorProvider.init();

            final AbstractUiCommand command = mock(AbstractUiCommand.class);
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);

            assertThrows(DefaultFrameworkException.class,
                    () -> executorProvider.getAppropriateExecutor(command, dependencies));
        }
    }

    @Nested
    class ExecutorMapState {

        @Test
        void executorMapIsNotNullAfterInit() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);

            executorProvider.init();

            assertNotNull(map);
        }
    }
}
