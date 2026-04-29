package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Include;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@RequiredArgsConstructor
@Component
public class IncludedScenarioRunnerImpl implements IncludedScenarioRunner {

    private static final String INCLUDE_CYCLE_DETECTED =
            "Include cycle detected: <%s> is already on the include stack";

    private final XMLParsers xmlParsers;
    private final FileSearcher fileSearcher;
    private final TestResourceSettings testResourceSettings;
    private final ResultUtil resultUtil;
    private final ConfigUtil configUtil;
    private final LogUtil logUtil;
    private final InterpreterProvider interpreterProvider;

    private final ThreadLocal<Deque<File>> includeStack = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public void run(final Include include,
                    final InterpreterDependencies dependencies,
                    final CommandResult result) {
        File includedFile = resolveIncludedFile(include);
        Deque<File> stack = includeStack.get();
        if (stack.contains(includedFile)) {
            throw new DefaultFrameworkException(INCLUDE_CYCLE_DETECTED, includedFile.getPath());
        }
        stack.push(includedFile);
        try {
            Scenario includedScenario = xmlParsers.forScenario().process(includedFile);
            List<CommandResult> subCommandsResult = result.getSubCommandsResult();
            for (AbstractCommand command : includedScenario.getCommands()) {
                processEachCommand(command, dependencies, subCommandsResult);
            }
            resultUtil.setExecutionResultIfSubCommandsFailed(result);
        } finally {
            stack.pop();
            if (stack.isEmpty()) {
                includeStack.remove();
            }
        }
    }

    private File resolveIncludedFile(final Include include) {
        File includedScenarioFolder = new File(testResourceSettings.getScenariosFolder(),
                include.getScenario());
        return fileSearcher.searchFileFromDir(includedScenarioFolder, TestResourceSettings.SCENARIO_FILENAME);
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
                interpreterProvider.getAppropriateInterpreter(command, dependencies);
        dependencies.getContext().getAutowireCapableBeanFactory().autowireBean(interpreter);
        return interpreter;
    }
}
