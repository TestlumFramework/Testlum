package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class SubCommandRunner {

    public void runCommands(final List<AbstractUiCommand> commandList,
                            final CommandResult result,
                            final ExecutorDependencies dependencies) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        commandList.forEach(command -> processEachCommand(command, subCommandsResult, dependencies));
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand command,
                                    final List<CommandResult> subCommandsResult,
                                    final ExecutorDependencies dependencies) {
        CommandResult commandResult = ResultUtil.newUiCommandResultInstance(dependencies.getPosition().get(), command);
        subCommandsResult.add(commandResult);
        executeUiCommand(command, commandResult, dependencies);
    }

    private void executeUiCommand(final AbstractUiCommand command,
                                  final CommandResult result,
                                  final ExecutorDependencies dependencies) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            getAppropriateExecutor(command, dependencies).apply(command, result);
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

    private AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand command,
                                                                         final ExecutorDependencies dependencies) {
        AbstractUiExecutor<AbstractUiCommand> executor = ExecutorProvider.getAppropriateExecutor(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(executor);
        return executor;
    }
}
