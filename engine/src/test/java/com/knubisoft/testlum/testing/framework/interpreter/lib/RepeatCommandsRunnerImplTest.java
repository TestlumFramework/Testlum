package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RepeatCommandsRunnerImpl} verifying command iteration
 * and empty list handling.
 */
@ExtendWith(MockitoExtension.class)
class RepeatCommandsRunnerImplTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private InterpreterProvider interpreterProvider;

    private RepeatCommandsRunnerImpl runner;

    @BeforeEach
    void setUp() {
        runner = new RepeatCommandsRunnerImpl(resultUtil, configUtil, logUtil, interpreterProvider);
    }

    @Nested
    class RunCommands {

        @Test
        void emptyCommandListDoesNothing() {
            final List<AbstractCommand> commands = new ArrayList<>();
            final CommandResult result = new CommandResult();
            final InterpreterDependencies dependencies = mock(InterpreterDependencies.class);
            final List<CommandResult> subResults = new ArrayList<>();

            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
            verify(interpreterProvider, never()).getAppropriateInterpreter(any(), any());
            assertTrue(subResults.isEmpty());
        }

        @Test
        void iteratesOverCommands() {
            final AbstractCommand cmd1 = mock(AbstractCommand.class);
            final AbstractCommand cmd2 = mock(AbstractCommand.class);
            final List<AbstractCommand> commands = List.of(cmd1, cmd2);
            final CommandResult result = new CommandResult();
            final CommandResult stepResult = new CommandResult();
            final InterpreterDependencies dependencies = mock(InterpreterDependencies.class);
            final AtomicInteger position = new AtomicInteger(0);
            when(dependencies.getPosition()).thenReturn(position);
            when(resultUtil.newCommandResultInstance(anyInt(), any(AbstractCommand.class)))
                    .thenReturn(stepResult);

            @SuppressWarnings("unchecked")
            final AbstractInterpreter<AbstractCommand> interpreter = mock(AbstractInterpreter.class);
            when(interpreterProvider.getAppropriateInterpreter(any(), eq(dependencies)))
                    .thenReturn(interpreter);

            final org.springframework.context.ApplicationContext ctx =
                    mock(org.springframework.context.ApplicationContext.class);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil, times(2))
                    .newCommandResultInstance(anyInt(), any(AbstractCommand.class));
            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
        }
    }
}
