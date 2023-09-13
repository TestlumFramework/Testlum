package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class RepeatCommandsRunnerImpl implements RepeatCommandRunner {

    public void runCommands(final List<AbstractCommand> commandList,
                            final InterpreterDependencies dependencies,
                            final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        commandList.forEach(command -> processEachCommand(command, dependencies, subCommandsResult));
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractCommand command,
                                    final InterpreterDependencies dependencies,
                                    final List<CommandResult> subCommandsResult) {
        CommandResult commandResult =
                ResultUtil.newCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
        subCommandsResult.add(commandResult);
        executeCommand(command, dependencies, commandResult);
    }

    private void executeCommand(final AbstractCommand command,
                                final InterpreterDependencies dependencies,
                                final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            getAppropriateInterpreter(command, dependencies).apply(command, result);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(result, e);
            LogUtil.logException(e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
    }

    private AbstractInterpreter<AbstractCommand> getAppropriateInterpreter(final AbstractCommand command,
                                                                           final InterpreterDependencies dependencies) {
        AbstractInterpreter<AbstractCommand> interpreter =
                InterpreterProvider.getAppropriateInterpreter(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(interpreter);
        return interpreter;
    }

    private void checkIfStopScenarioOnFailure(final Exception e) {
        if (GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure()) {
            throw new DefaultFrameworkException(e);
        }
    }
}
