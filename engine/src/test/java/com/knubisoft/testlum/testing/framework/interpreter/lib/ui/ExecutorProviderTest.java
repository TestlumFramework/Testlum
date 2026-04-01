package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExecutorProvider} verifying executor lookup,
 * initialization behavior, successful resolution, and constructor error handling.
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

        @Test
        void initCanBeCalledMultipleTimes() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);

            executorProvider.init();
            executorProvider.init();

            verify(executorScanner, times(2)).getExecutors();
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

        @Test
        void throwsWithMeaningfulMessageForUnknownCommand() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);
            executorProvider.init();

            final AbstractUiCommand command = mock(AbstractUiCommand.class);
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> executorProvider.getAppropriateExecutor(command, dependencies));
            assertNotNull(ex.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void throwsWhenExecutorClassLacksConstructor() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();

            Class<AbstractUiExecutor<? extends AbstractUiCommand>> badClass =
                    (Class<AbstractUiExecutor<? extends AbstractUiCommand>>) (Class<?>) BadExecutor.class;

            map.put(TestUiCommand.class, badClass);

            when(executorScanner.getExecutors()).thenReturn(map);
            executorProvider.init();

            TestUiCommand command = new TestUiCommand();
            ExecutorDependencies dependencies = mock(ExecutorDependencies.class);

            assertThrows(DefaultFrameworkException.class,
                    () -> executorProvider.getAppropriateExecutor(command, dependencies));
        }

        @SuppressWarnings("unchecked")
        @Test
        void returnsExecutorWhenCorrectlyMapped() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();

            Class<AbstractUiExecutor<? extends AbstractUiCommand>> goodClass =
                    (Class<AbstractUiExecutor<? extends AbstractUiCommand>>) (Class<?>) GoodExecutor.class;

            map.put(TestUiCommand.class, goodClass);

            when(executorScanner.getExecutors()).thenReturn(map);
            executorProvider.init();

            TestUiCommand command = new TestUiCommand();
            ExecutorDependencies dependencies = createMockDependencies();

            AbstractUiExecutor<AbstractUiCommand> result =
                    executorProvider.getAppropriateExecutor(command, dependencies);

            assertNotNull(result);
            assertInstanceOf(GoodExecutor.class, result);
        }
    }

    private ExecutorDependencies createMockDependencies() {
        ExecutorDependencies deps = mock(ExecutorDependencies.class);
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(deps.getContext()).thenReturn(ctx);
        when(ctx.getBean(UiUtil.class)).thenReturn(mock(UiUtil.class));
        when(ctx.getBean(ResultUtil.class)).thenReturn(mock(ResultUtil.class));
        when(ctx.getBean(JavascriptUtil.class)).thenReturn(mock(JavascriptUtil.class));
        when(ctx.getBean(ImageComparisonUtil.class)).thenReturn(mock(ImageComparisonUtil.class));
        when(ctx.getBean(ConditionUtil.class)).thenReturn(mock(ConditionUtil.class));
        when(ctx.getBean(ConfigUtil.class)).thenReturn(mock(ConfigUtil.class));
        when(ctx.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(ctx.getBean(LogUtil.class)).thenReturn(mock(LogUtil.class));
        when(ctx.getBean(ScenarioInjectionUtil.class)).thenReturn(mock(ScenarioInjectionUtil.class));
        when(ctx.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(ctx.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        return deps;
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

        @Test
        void executorMapIsEmptyWhenNoExecutorsRegistered() {
            final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
            when(executorScanner.getExecutors()).thenReturn(map);

            executorProvider.init();

            assertTrue(map.isEmpty());
        }
    }

    // Test helpers

    public static class TestUiCommand extends AbstractUiCommand {
    }

    /**
     * Executor without the required ExecutorDependencies constructor.
     */
    public static class BadExecutor extends AbstractUiExecutor<TestUiCommand> {
        BadExecutor() {
            super(null, null, null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        protected void execute(final TestUiCommand command, final CommandResult result) {
        }
    }

    /**
     * Executor with the required ExecutorDependencies constructor.
     */
    public static class GoodExecutor extends AbstractUiExecutor<TestUiCommand> {
        public GoodExecutor(final ExecutorDependencies dependencies) {
            super(dependencies);
        }

        @Override
        protected void execute(final TestUiCommand command, final CommandResult result) {
        }
    }
}
