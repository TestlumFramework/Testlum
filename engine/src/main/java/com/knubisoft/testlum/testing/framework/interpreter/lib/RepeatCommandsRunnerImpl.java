package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RepeatCommandsRunnerImpl implements RepeatCommandRunner {

    private final ResultUtil resultUtil;
    private final ConfigUtil configUtil;
    private final LogUtil logUtil;

    public void runCommands(final List<AbstractCommand> commandList,
                            final InterpreterDependencies dependencies,
                            final CommandResult result,
                            final List<CommandResult> subCommandsResult) {
        commandList.forEach(command -> processEachCommand(command, dependencies, subCommandsResult));
        resultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractCommand command,
                                    final InterpreterDependencies dependencies,
                                    final List<CommandResult> subCommandsResult) {
        CommandResult commandResult =
                resultUtil.newCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
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
            resultUtil.setExceptionResult(result, e);
            logUtil.logException(e);
            configUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getDuration().toMillis();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            logUtil.logExecutionTime(execTime, command);
        }
    }

    private AbstractInterpreter<AbstractCommand> getAppropriateInterpreter(final AbstractCommand command,
                                                                           final InterpreterDependencies dependencies) {
        AbstractInterpreter<AbstractCommand> interpreter =
                InterpreterProvider.getAppropriateInterpreter(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(interpreter);
        return interpreter;
    }
}
