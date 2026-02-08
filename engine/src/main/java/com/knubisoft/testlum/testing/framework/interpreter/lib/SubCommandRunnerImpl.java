package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.WebAssert;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SLOW_COMMAND_PROCESSING;
import static java.util.Objects.nonNull;

@Component
public class SubCommandRunnerImpl implements SubCommandRunner {

    public void runCommands(final List<AbstractUiCommand> commandList,
                            final CommandResult result,
                            final ExecutorDependencies dependencies) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        commandList.forEach(command -> processEachCommand(command, subCommandsResult, dependencies));
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    public void runCommands(final List<AbstractUiCommand> commandList,
                            final ExecutorDependencies dependencies,
                            final CommandResult result,
                            final List<CommandResult> subCommandsResult) {
        commandList.forEach(command -> processEachCommand(command, subCommandsResult, dependencies));
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand command,
                                    final List<CommandResult> subCommandsResult,
                                    final ExecutorDependencies dependencies) {
        CommandResult commandResult = createCommandResult(command, dependencies);
        subCommandsResult.add(commandResult);
        executeUiCommand(command, commandResult, dependencies);
    }

    private CommandResult createCommandResult(final AbstractUiCommand command,
                                              final ExecutorDependencies dependencies) {
        return command instanceof WebAssert ? ResultUtil.newUiCommandResultInstance(0, command)
                : ResultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
    }

    private void executeUiCommand(final AbstractUiCommand command,
                                  final CommandResult result,
                                  final ExecutorDependencies dependencies) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            getAppropriateExecutor(command, dependencies).apply(command, result);
        } catch (Exception e) {
            processException(e, result);
        } finally {
            checkExecutionTime(stopWatch, command, result);
        }
    }

    private AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand command,
                                                                         final ExecutorDependencies dependencies) {
        AbstractUiExecutor<AbstractUiCommand> executor = ExecutorProvider.getAppropriateExecutor(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(executor);
        return executor;
    }

    private void checkExecutionTime(final StopWatch stopWatch,
                                    final AbstractUiCommand command,
                                    final CommandResult result) {
        long execTime = stopWatch.getDuration().toMillis();
        stopWatch.stop();
        result.setExecutionTime(execTime);
        LogUtil.logExecutionTime(execTime, command);
        Integer threshold = command.getThreshold();
        if (nonNull(threshold) && execTime > threshold) {
            processException(new DefaultFrameworkException(SLOW_COMMAND_PROCESSING, execTime, threshold), result);
        }
    }

    private void processException(final Exception e, final CommandResult result) {
        ResultUtil.setExceptionResult(result, e);
        LogUtil.logException(e);
        ConfigUtil.checkIfStopScenarioOnFailure(e);
    }
}
