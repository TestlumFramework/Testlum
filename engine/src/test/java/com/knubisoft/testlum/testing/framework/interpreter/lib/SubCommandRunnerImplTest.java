package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SubCommandRunnerImpl} verifying UI command iteration
 * and empty list handling.
 */
@ExtendWith(MockitoExtension.class)
class SubCommandRunnerImplTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ExecutorProvider executorProvider;

    private SubCommandRunnerImpl runner;

    @BeforeEach
    void setUp() {
        runner = new SubCommandRunnerImpl(resultUtil, configUtil, logUtil, executorProvider);
    }

    @Nested
    class RunCommandsWithAutoSubResults {

        @Test
        void emptyCommandListDoesNothing() {
            final List<AbstractUiCommand> commands = new ArrayList<>();
            final CommandResult result = new CommandResult();
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);

            runner.runCommands(commands, result, dependencies);

            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
            verify(executorProvider, never()).getAppropriateExecutor(any(), any());
            assertNotNull(result.getSubCommandsResult());
            assertTrue(result.getSubCommandsResult().isEmpty());
        }

        @Test
        void iteratesOverUiCommands() {
            final AbstractUiCommand cmd1 = mock(AbstractUiCommand.class);
            final AbstractUiCommand cmd2 = mock(AbstractUiCommand.class);
            final List<AbstractUiCommand> commands = List.of(cmd1, cmd2);
            final CommandResult result = new CommandResult();
            final CommandResult stepResult = new CommandResult();
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);
            final AtomicInteger position = new AtomicInteger(0);
            when(dependencies.getPosition()).thenReturn(position);
            when(resultUtil.newUiCommandResultInstance(anyInt(), any(AbstractUiCommand.class))).thenReturn(stepResult);

            @SuppressWarnings("unchecked")
            final AbstractUiExecutor<AbstractUiCommand> executor = mock(AbstractUiExecutor.class);
            when(executorProvider.getAppropriateExecutor(any(), eq(dependencies))).thenReturn(executor);

            final org.springframework.context.ApplicationContext ctx =
                    mock(org.springframework.context.ApplicationContext.class);
            when(dependencies.getContext()).thenReturn(ctx);

            runner.runCommands(commands, result, dependencies);

            verify(resultUtil, times(2)).newUiCommandResultInstance(anyInt(), any(AbstractUiCommand.class));
            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
        }
    }

    @Nested
    class RunCommandsWithExplicitSubResults {

        @Test
        void emptyCommandListDoesNothing() {
            final List<AbstractUiCommand> commands = new ArrayList<>();
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);
            final CommandResult result = new CommandResult();
            final List<CommandResult> subResults = new ArrayList<>();

            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
            verify(executorProvider, never()).getAppropriateExecutor(any(), any());
            assertTrue(subResults.isEmpty());
        }

        @Test
        void iteratesOverUiCommandsWithExplicitSubResults() {
            final AbstractUiCommand cmd1 = mock(AbstractUiCommand.class);
            final List<AbstractUiCommand> commands = List.of(cmd1);
            final CommandResult result = new CommandResult();
            final CommandResult stepResult = new CommandResult();
            final ExecutorDependencies dependencies = mock(ExecutorDependencies.class);
            final AtomicInteger position = new AtomicInteger(0);
            when(dependencies.getPosition()).thenReturn(position);
            when(resultUtil.newUiCommandResultInstance(anyInt(), any(AbstractUiCommand.class))).thenReturn(stepResult);

            @SuppressWarnings("unchecked")
            final AbstractUiExecutor<AbstractUiCommand> executor = mock(AbstractUiExecutor.class);
            when(executorProvider.getAppropriateExecutor(any(), eq(dependencies))).thenReturn(executor);

            final org.springframework.context.ApplicationContext ctx =
                    mock(org.springframework.context.ApplicationContext.class);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil).newUiCommandResultInstance(anyInt(), any(AbstractUiCommand.class));
            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
        }
    }
}
