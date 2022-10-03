package com.knubisoft.cott.testing.framework.scenario;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.ui.MobilebrowserDriverFactory;
import com.knubisoft.cott.testing.framework.configuration.ui.NativeDriverFactory;
import com.knubisoft.cott.testing.framework.configuration.ui.WebDriverFactory;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.StopSignalException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.CommandToInterpreterMap;
import com.knubisoft.cott.testing.framework.interpreter.lib.Drivers;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.report.ScenarioResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.model.ScenarioArguments;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FUNCTION_FOR_COMMAND_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.MISSING_CONSTRUCTOR;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXECUTION_STOP_SIGNAL_LOG;


@Slf4j
@RequiredArgsConstructor
public class ScenarioRunner {
    private static final AtomicInteger SCENARIO_ID_GENERATOR = new AtomicInteger();
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private static final ScenarioResult SCENARIO_RESULT = new ScenarioResult();

    private final ScenarioArguments scenarioArguments;
    private final ApplicationContext ctx;
    private InterpreterDependencies dependencies;
    private boolean stopScenarioOnFailure;
    private CommandToInterpreterMap cmdToInterpreterMap;

    private final Drivers drivers = new Drivers();

    public ScenarioResult run() {
        prepare();
        prepareScenarioResult();
        LogUtil.logScenarioDetails(scenarioArguments, SCENARIO_ID_GENERATOR);
        runScenarioCommands();
        return SCENARIO_RESULT;
    }

    private void prepare() {
        this.dependencies = createDependencies();
        this.stopScenarioOnFailure = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
        this.cmdToInterpreterMap = createClassToInterpreterMap(dependencies);
    }

    private void prepareScenarioResult() {
        Scenario scenario = scenarioArguments.getScenario();
        SCENARIO_RESULT.setId(SCENARIO_ID_GENERATOR.incrementAndGet());
        SCENARIO_RESULT.setPath(StringUtils.remove(scenarioArguments.getFile().getPath(), System.getProperty("PWD")));
        SCENARIO_RESULT.setName(scenario.getOverview().getName());
        SCENARIO_RESULT.setOverview(scenario.getOverview());
        SCENARIO_RESULT.setTags(scenario.getTags());
        SCENARIO_RESULT.setBrowser(scenarioArguments.getBrowser());
        SCENARIO_RESULT.setMobilebrowserDevice(scenarioArguments.getMobilebrowserDevice());
        SCENARIO_RESULT.setNativeDevice(scenarioArguments.getNativeDevice());
        SCENARIO_RESULT.setSuccess(true);
    }

    private void runScenarioCommands() {
        try {
            runCommands(scenarioArguments.getScenario().getCommands());
        } catch (StopSignalException ignore) {
            log.error(EXECUTION_STOP_SIGNAL_LOG);
        } finally {
            if (scenarioArguments.isContainsUiSteps()) {
                dependencies.getDrivers().getDriversList().stream().filter(Objects::nonNull).forEach(WebDriver::quit);
            }
        }
    }

    private void runCommands(final List<AbstractCommand> commands) {
        for (AbstractCommand command : commands) {
            processCommand(command, result -> {
                SCENARIO_RESULT.getCommands().add(result);
                handleException(result);
            });
        }
    }

    private void processCommand(final AbstractCommand command, final CommandCallback callback) {
        CommandResult result = prepareCommandResult(command);
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            executeCommand(command, result);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
        callback.onCommandExecuted(result);
    }

    private void executeCommand(final AbstractCommand command, final CommandResult result) {
        try {
            getInterpreterOrThrow(command).apply(command, result);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(result, e);
        }
    }

    private CommandResult prepareCommandResult(final AbstractCommand command) {
        CommandResult result = new CommandResult();
        result.setId(ID_GENERATOR.incrementAndGet());
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
        if (SCENARIO_RESULT.getCause() == null) {
            SCENARIO_RESULT.setCause(ex);
            SCENARIO_RESULT.setSuccess(false);
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
                ID_GENERATOR
        );
    }

    private InterpreterDependencies createDependenciesWithUI() {
        return new InterpreterDependencies(
                createDrivers(),
                ctx,
                scenarioArguments.getFile(),
                new ScenarioContext(scenarioArguments.getVariation()),
                ID_GENERATOR
        );
    }

    private Drivers createDrivers() {
        if (scenarioArguments.getBrowser() != null) {
            drivers.setWebDriver(WebDriverFactory.createDriver(scenarioArguments.getBrowser()));
        }
        if (scenarioArguments.getMobilebrowserDevice() != null) {
            drivers.setMobilebrowserDriwer(MobilebrowserDriverFactory
                    .createDriver(scenarioArguments.getMobilebrowserDevice()));
        }
        if (scenarioArguments.getNativeDevice() != null) {
            drivers.setNativeDriver(NativeDriverFactory.createDriver(scenarioArguments.getNativeDevice()));
        }
        return drivers;
    }

    public interface CommandCallback {
        void onCommandExecuted(CommandResult result);
    }
}
