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
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

            final ApplicationContext ctx = mock(ApplicationContext.class);
            final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
            when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil, times(2))
                    .newCommandResultInstance(anyInt(), any(AbstractCommand.class));
            verify(resultUtil).setExecutionResultIfSubCommandsFailed(result);
            assertEquals(2, subResults.size());
        }

        @Test
        void exceptionDuringExecutionIsHandled() {
            final AbstractCommand cmd = mock(AbstractCommand.class);
            final List<AbstractCommand> commands = List.of(cmd);
            final CommandResult result = new CommandResult();
            final CommandResult stepResult = new CommandResult();
            final InterpreterDependencies dependencies = mock(InterpreterDependencies.class);
            final AtomicInteger position = new AtomicInteger(0);
            when(dependencies.getPosition()).thenReturn(position);
            when(resultUtil.newCommandResultInstance(anyInt(), any(AbstractCommand.class)))
                    .thenReturn(stepResult);

            @SuppressWarnings("unchecked")
            final AbstractInterpreter<AbstractCommand> interpreter = mock(AbstractInterpreter.class);
            RuntimeException ex = new RuntimeException("interpreter failed");
            doThrow(ex).when(interpreter).apply(any(), any());
            when(interpreterProvider.getAppropriateInterpreter(any(), eq(dependencies)))
                    .thenReturn(interpreter);

            final ApplicationContext ctx = mock(ApplicationContext.class);
            final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
            when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil).setExceptionResult(stepResult, ex);
            verify(logUtil).logException(ex);
            verify(configUtil).checkIfStopScenarioOnFailure(ex);
        }

        @Test
        void executionTimeIsLogged() {
            final AbstractCommand cmd = mock(AbstractCommand.class);
            final List<AbstractCommand> commands = List.of(cmd);
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

            final ApplicationContext ctx = mock(ApplicationContext.class);
            final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
            when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(logUtil).logExecutionTime(anyLong(), eq(cmd));
        }

        @Test
        void positionIsIncremented() {
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

            final ApplicationContext ctx = mock(ApplicationContext.class);
            final AutowireCapableBeanFactory beanFactory = mock(AutowireCapableBeanFactory.class);
            when(ctx.getAutowireCapableBeanFactory()).thenReturn(beanFactory);
            when(dependencies.getContext()).thenReturn(ctx);

            final List<CommandResult> subResults = new ArrayList<>();
            runner.runCommands(commands, dependencies, result, subResults);

            verify(resultUtil).newCommandResultInstance(eq(1), eq(cmd1));
            verify(resultUtil).newCommandResultInstance(eq(2), eq(cmd2));
        }
    }
}
