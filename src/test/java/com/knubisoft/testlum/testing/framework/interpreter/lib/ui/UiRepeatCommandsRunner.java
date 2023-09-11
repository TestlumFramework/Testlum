package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UiRepeatCommandsRunner {

    public void runCommands(final List<AbstractUiCommand> commandList,
                            final ExecutorDependencies dependencies,
                            final CommandResult result,
                            final List<CommandResult> subCommandResult) {
        commandList.forEach(command -> processEachCommand(command, dependencies, subCommandResult));
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand command,
                                    final ExecutorDependencies dependencies,
                                    final List<CommandResult> subCommandsResult) {
        CommandResult commandResult =
                ResultUtil.newCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
        subCommandsResult.add(commandResult);
        executeCommand(command, dependencies, commandResult);
    }

    private void executeCommand(final AbstractUiCommand command,
                                final ExecutorDependencies dependencies,
                                final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            getAppropriateInterpreter(command, dependencies).apply(command, result);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(result, e);
            LogUtil.logException(e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
    }

    private AbstractUiExecutor<AbstractUiCommand> getAppropriateInterpreter(final AbstractUiCommand command,
                                                                           final ExecutorDependencies dependencies) {
        AbstractUiExecutor<AbstractUiCommand> executor =
                ExecutorProvider.getAppropriateExecutor(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(executor);
        return executor;
    }
}
