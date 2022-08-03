package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.ui.WebDriverFactory;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.StopSignalException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CommandToInterpreterMap;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.report.ScenarioResult;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXECUTION_STOP_SIGNAL_LOG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FUNCTION_FOR_COMMAND_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.MISSING_CONSTRUCTOR;


@Slf4j
@RequiredArgsConstructor
public class ScenarioRunner {
    private static final AtomicInteger SCENARIO_ID_GENERATOR = new AtomicInteger();
    private final ScenarioArguments scenarioArguments;
    private final ApplicationContext ctx;
    private final ScenarioResult scenarioResult = new ScenarioResult();
    private final AtomicInteger idGenerator = new AtomicInteger();
    private InterpreterDependencies dependencies;
    private boolean stopScenarioOnFailure;
    private CommandToInterpreterMap cmdToInterpreterMap;

    public ScenarioResult run() {
        prepare();
        prepareReport();
        LogUtil.logScenarioDetails(scenarioArguments, SCENARIO_ID_GENERATOR);
        runScenarioCommands();
        return scenarioResult;
    }

    private void prepare() {
        this.dependencies = createDependencies();
        this.stopScenarioOnFailure = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
        this.cmdToInterpreterMap = createClassToInterpreterMap(dependencies);
    }

    private void prepareReport() {
        Scenario scenario = scenarioArguments.getScenario();
        scenarioResult.setId(SCENARIO_ID_GENERATOR.incrementAndGet());
        scenarioResult.setPath(StringUtils.remove(scenarioArguments.getFile().getPath(), System.getProperty("PWD")));
        scenarioResult.setName(scenario.getOverview().getName());
        scenarioResult.setOverview(scenario.getOverview());
        scenarioResult.setTags(scenario.getTags());
        scenarioResult.setSuccess(true);
    }

    private void runScenarioCommands() {
        try {
            runCommands(scenarioArguments.getScenario().getCommands());
        } catch (StopSignalException ignore) {
            log.info(EXECUTION_STOP_SIGNAL_LOG);
        } finally {
            if (scenarioArguments.isContainsUiSteps()) {
                dependencies.getWebDriver().manage().deleteAllCookies();
                dependencies.getWebDriver().quit();
            }
        }
    }

    private void runCommands(final List<AbstractCommand> commands) {
        for (AbstractCommand command : commands) {
            execCommand(command, result -> {
                scenarioResult.getCommands().add(result);
                handleException(result);
            });
        }
    }

    private void execCommand(final AbstractCommand command, final CommandCallback callback) {
        CommandResult result = prepareCommandResult(command);
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            execute(command, result);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
        callback.onCommandExecuted(result);
    }

    private void execute(final AbstractCommand command, final CommandResult result) {
        try {
            getInterpreterOrThrow(command).apply(command, result);
        } catch (StopSignalException e) {
            throw e;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setException(e);
        }
    }

    private CommandResult prepareCommandResult(final AbstractCommand command) {
        CommandResult result = new CommandResult();
        result.setId(idGenerator.incrementAndGet());
        result.setCommandKey(command.getClass().getSimpleName());
        result.setComment(command.getComment());
        result.setSuccess(true);
        return result;
    }

    private AbstractInterpreter<AbstractCommand> getInterpreterOrThrow(final AbstractCommand command) {
        AbstractInterpreter<? extends AbstractCommand> interpreter = cmdToInterpreterMap.get(command.getClass());
        if (interpreter == null) {
            throw new DefaultFrameworkException(FUNCTION_FOR_COMMAND_NOT_FOUND, command.getClass());
        }
        return (AbstractInterpreter<AbstractCommand>) interpreter;
    }

    @SneakyThrows
    private void handleException(final CommandResult result) {
        Exception ex = result.getException();
        if (ex != null) {
            LogUtil.logException(ex);
            fillReportException(ex.getMessage());
            if (stopScenarioOnFailure) {
                throw new StopSignalException();
            }
        }
    }

    private void fillReportException(final String ex) {
        if (scenarioResult.getCause() == null) {
            scenarioResult.setCause(ex);
            scenarioResult.setSuccess(false);
        }
    }

    @SneakyThrows
    private CommandToInterpreterMap createClassToInterpreterMap(final InterpreterDependencies dependencies) {
        CommandToInterpreterMap interpreterMap = new CommandToInterpreterMap();
        InterpreterScanner.getInterpreters().forEach(
                (key, value) -> interpreterMap.put(key, createInterpreterInstance(dependencies, value)));
        return interpreterMap;
    }

    private AbstractInterpreter<? extends AbstractCommand> createInterpreterInstance(
            final InterpreterDependencies dependencies,
            final Class<AbstractInterpreter<? extends AbstractCommand>> value) {
        try {
            AbstractInterpreter<? extends AbstractCommand> instance =
                    value.getConstructor(InterpreterDependencies.class).newInstance(dependencies);
            ctx.getAutowireCapableBeanFactory().autowireBean(instance);
            return instance;
        } catch (Exception e) {
            throw new DefaultFrameworkException(MISSING_CONSTRUCTOR, value);
        }
    }

    private InterpreterDependencies createDependencies() {
        if (scenarioArguments.isContainsUiSteps()) {
            return createDependenciesWithUI();
        } else {
            return createDependenciesWithoutUI();
        }
    }

    private InterpreterDependencies createDependenciesWithoutUI() {
        return new InterpreterDependencies(
                ctx,
                scenarioArguments.getFile(),
                new ScenarioContext(scenarioArguments.getVariation()),
                idGenerator
        );
    }

    private InterpreterDependencies createDependenciesWithUI() {
        return new InterpreterDependencies(
                WebDriverFactory.createDriver(scenarioArguments.getBrowser()),
                ctx,
                scenarioArguments.getFile(),
                new ScenarioContext(scenarioArguments.getVariation()),
                idGenerator
        );
    }

    private interface CommandCallback {
        void onCommandExecuted(CommandResult result);
    }
}
