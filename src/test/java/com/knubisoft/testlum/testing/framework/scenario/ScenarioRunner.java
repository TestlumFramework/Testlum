package com.knubisoft.testlum.testing.framework.scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.ui.MobilebrowserDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.NativeDriverFactory;
import com.knubisoft.testlum.testing.framework.configuration.ui.WebDriverFactory;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.StopSignalException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CommandToInterpreterMap;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterScanner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.MockDriver;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.MobileUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.ScenarioArguments;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FUNCTION_FOR_COMMAND_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.MISSING_CONSTRUCTOR;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.MOBILEBROWSER_DRIVER_NOT_INIT;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NATIVE_DRIVER_NOT_INIT;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.WEB_DRIVER_NOT_INIT;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXECUTION_STOP_SIGNAL_LOG;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;


@Slf4j
@RequiredArgsConstructor
public class ScenarioRunner {

    private static final AtomicInteger SCENARIO_ID_GENERATOR = new AtomicInteger();
    private final AtomicInteger idGenerator = new AtomicInteger();
    private final ScenarioResult scenarioResult = new ScenarioResult();

    private final ScenarioArguments scenarioArguments;
    private final ApplicationContext ctx;

    private InterpreterDependencies dependencies;
    private boolean stopScenarioOnFailure;
    private CommandToInterpreterMap cmdToInterpreterMap;

    public ScenarioResult run() {
        prepare();
        injectScenario();
        prepareScenarioResult();
        runScenarioCommands();
        return scenarioResult;
    }

    private void prepare() {
        this.dependencies = createDependencies();
        this.stopScenarioOnFailure = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
        this.cmdToInterpreterMap = createClassToInterpreterMap(dependencies);
    }

    private void injectScenario() {
        Scenario scenario = scenarioArguments.getScenario();
        scenario.setOverview(InjectionUtil.injectObject(scenario.getOverview(), new TypeReference<Overview>() { },
                dependencies.getScenarioContext()));
    }

    private void prepareScenarioResult() {
        Scenario scenario = scenarioArguments.getScenario();
        scenarioResult.setId(SCENARIO_ID_GENERATOR.incrementAndGet());
        scenarioResult.setOverview(scenario.getOverview());
        scenarioResult.setName(scenario.getOverview().getName());
        scenarioResult.setTags(scenario.getTags());
        scenarioResult.setPath(scenarioArguments.getFile().getPath());
        scenarioResult.setBrowser(scenarioArguments.getBrowser());
        scenarioResult.setMobilebrowserDevice(scenarioArguments.getMobilebrowserDevice());
        scenarioResult.setNativeDevice(scenarioArguments.getNativeDevice());
        scenarioResult.setSuccess(true);
        scenarioResult.setEnvironment(scenarioArguments.getEnvironment());
    }

    private void runScenarioCommands() {
        LogUtil.logScenarioDetails(scenarioArguments, scenarioResult.getId());
        try {
            runCommands(scenarioArguments.getScenario().getCommands());
        } catch (StopSignalException ignore) {
            log.error(EXECUTION_STOP_SIGNAL_LOG);
        } finally {
            if (scenarioArguments.isContainsUiSteps()) {
                dependencies.getNativeDriver().quit();
                dependencies.getWebDriver().quit();
                dependencies.getMobilebrowserDriver().quit();
            }
        }
    }

    private void runCommands(final List<AbstractCommand> commands) {
        for (AbstractCommand command : commands) {
            processCommand(command, result -> {
                scenarioResult.getCommands().add(result);
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
        result.setId(idGenerator.incrementAndGet());
        result.setCommandKey(command.getClass().getSimpleName());
        result.setComment(command.getComment());
        result.setSuccess(true);
        return result;
    }

    @SuppressWarnings("unchecked")
    private AbstractInterpreter<AbstractCommand> getInterpreterOrThrow(final AbstractCommand command) {
        AbstractInterpreter<? extends AbstractCommand> interpreter = cmdToInterpreterMap.get(command.getClass());
        if (isNull(interpreter)) {
            throw new DefaultFrameworkException(FUNCTION_FOR_COMMAND_NOT_FOUND, command.getClass());
        }
        return (AbstractInterpreter<AbstractCommand>) interpreter;
    }

    private void handleException(final CommandResult result) {
        Exception ex = result.getException();
        if (nonNull(ex)) {
            LogUtil.logException(ex);
            fillReportException(ex);
            if (stopScenarioOnFailure) {
                throw new StopSignalException();
            }
        }
    }

    private void fillReportException(final Exception ex) {
        if (isBlank(scenarioResult.getCause())) {
            String cause = isBlank(ex.getMessage()) ? ex.getClass().getSimpleName() : ex.getMessage();
            scenarioResult.setCause(cause);
            scenarioResult.setSuccess(false);
        }
    }

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
        return InterpreterDependencies.builder()
                .context(ctx)
                .file(scenarioArguments.getFile())
                .scenarioContext(new ScenarioContext(scenarioArguments.getVariations()))
                .position(idGenerator)
                .environment(scenarioArguments.getEnvironment())
                .webDriver(createWebDriver())
                .mobilebrowserDriver(createMobilebrowserDriver())
                .nativeDriver(createNativeDriver())
                .build();
    }

    private WebDriver createWebDriver() {
        return BrowserUtil.getBrowserBy(scenarioArguments.getEnvironment(), scenarioArguments.getBrowser())
                .map(WebDriverFactory::createDriver)
                .orElse(new MockDriver(WEB_DRIVER_NOT_INIT));
    }

    private WebDriver createMobilebrowserDriver() {
        return MobileUtil.getMobilebrowserDeviceBy(scenarioArguments.getEnvironment(),
                        scenarioArguments.getMobilebrowserDevice())
                .map(MobilebrowserDriverFactory::createDriver)
                .orElse(new MockDriver(MOBILEBROWSER_DRIVER_NOT_INIT));
    }

    private WebDriver createNativeDriver() {
        return MobileUtil.getNativeDeviceBy(scenarioArguments.getEnvironment(), scenarioArguments.getNativeDevice())
                .map(NativeDriverFactory::createDriver)
                .orElse(new MockDriver(NATIVE_DRIVER_NOT_INIT));
    }

    public interface CommandCallback {
        void onCommandExecuted(CommandResult result);
    }
}
