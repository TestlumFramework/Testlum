package com.knubisoft.e2e.testing.framework.scenario;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.exception.StopSignalException;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.CommandToInterpreterMap;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.framework.WebDriverFactory;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.report.ScenarioResult;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.scenario.Repeat;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXCEPTION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXECUTE_SCENARIO_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXECUTION_STOP_SIGNAL_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.FUNCTION_FOR_COMMAND_NOT_FOUND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.MISSING_CONSTRUCTOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.OVERVIEW_LOG;


@Slf4j
@RequiredArgsConstructor
public class ScenarioRunner {
    private static final AtomicInteger SCENARIO_ID_GENERATOR = new AtomicInteger();
    private final File file;
    private final Scenario scenario;
    private final String browserVersionElement;
    private final BrowserSettings browserSettings;
    private final Map<String, String> variation;
    private final ApplicationContext ctx;
    private final ScenarioResult scenarioResult = new ScenarioResult();
    private final AtomicInteger idGenerator = new AtomicInteger();
    private InterpreterDependencies dependencies;
    private boolean stopScenarioOnFailure;
    private CommandToInterpreterMap cmdToInterpreterMap;

    public ScenarioResult run() {
        log.info("--------------------------------------------------");
        log.info(EXECUTE_SCENARIO_LOG, file.getAbsolutePath());
        prepare();
        logOverview();
        prepareReport();
        runScenarioCommands(scenario.getCommands());
        return scenarioResult;
    }

    private void prepare() {
        this.dependencies = createDependencies(ctx);
        this.stopScenarioOnFailure = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
        this.cmdToInterpreterMap = createClassToInterpreterMap(dependencies);
    }

    private void prepareReport() {
        scenarioResult.setId(SCENARIO_ID_GENERATOR.incrementAndGet());
        scenarioResult.setPath(StringUtils.remove(file.getPath(), System.getProperty("PWD")));
        scenarioResult.setName(scenario.getOverview().getName());
        scenarioResult.setOverview(scenario.getOverview());
        scenarioResult.setTags(scenario.getTags());
        scenarioResult.setSuccess(true);
    }

    private void runScenarioCommands(final List<AbstractCommand> commands) {
        try {
            runCommands(commands);
        } catch (StopSignalException ignore) {
            log.info(EXECUTION_STOP_SIGNAL_LOG);
        } finally {
            dependencies.getWebDriver().quit();
        }
    }

    private void runCommands(final List<AbstractCommand> commands) {
        for (AbstractCommand command : commands) {
            runCommand(command, result -> {
                scenarioResult.getCommands().add(result);
                handleException(result);
            });
        }
    }

    private void runCommand(final AbstractCommand command, final CommandCallback callback) {
        if (command instanceof Repeat) {
            runRepeatCommand((Repeat) command, callback);
        } else {
            execCommand(command, callback);
        }
    }

    private void runRepeatCommand(final Repeat repeat, final CommandCallback callback) {
        int times = Integer.parseInt(dependencies.getScenarioContext().inject(repeat.getTimes()));
        for (int i = 0; i < times; i++) {
            repeat.getCommands().forEach(command -> runCommand(command, callback));
        }
    }

    private void execCommand(final AbstractCommand command, final CommandCallback callback) {
        CommandResult result = prepareCommandResult(command);
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            execute(command, result);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
        }
        callback.onCommandExecuted(result);
    }

    //CHECKSTYLE:OFF
    private void execute(final AbstractCommand command, final CommandResult result) {
        try {
            getInterpreterOrThrow(command).apply(command, result);
        } catch (StopSignalException e) {
            throw e;
        } catch (Exception e) {
            result.setSuccess(false);
            result.setCause(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }
    //CHECKSTYLE:ON

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

    //CHECKSTYLE:OFF
    @SneakyThrows
    private void handleException(final CommandResult result) {
        String ex = result.getCause();
        if (ex != null) {
            log.error("----------------    EXCEPTION    -----------------");
            log.error(EXCEPTION_LOG, ex);
            log.error("--------------------------------------------------");
            fillReportException(ex);
            if (stopScenarioOnFailure) {
                throw new StopSignalException();
            }
        }
    }

    //CHECKSTYLE:ON
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

    private void logOverview() {
        Overview overview = scenario.getOverview();
        if (overview != null) {
            log.info(OVERVIEW_LOG);
            logIfExist(overview.getDescription(), overview.getDeveloper(), overview.getJira());
            overview.getLink().stream().filter(StringUtils::isNotBlank).forEach(log::info);
        }
    }

    private void logIfExist(final String... msg) {
        for (String each : msg) {
            if (each != null) {
                log.info(each);
            }
        }
    }

    private InterpreterDependencies createDependencies(final ApplicationContext ctx) {
        return new InterpreterDependencies(
                WebDriverFactory.create(browserVersionElement, browserSettings),
                ctx,
                file,
                new ScenarioContext(variation),
                idGenerator
        );
    }

    private interface CommandCallback {
        void onCommandExecuted(CommandResult result);
    }
}
