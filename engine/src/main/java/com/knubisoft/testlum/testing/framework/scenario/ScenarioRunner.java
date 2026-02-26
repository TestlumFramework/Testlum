package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.configuration.ui.MobileBrowserDriverFactory;
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
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.*;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXECUTION_STOP_SIGNAL_LOG;

@Slf4j
public class ScenarioRunner {

    private static final AtomicInteger SCENARIO_ID_GENERATOR = new AtomicInteger();

    private final AtomicInteger idGenerator = new AtomicInteger();
    private final ScenarioResult scenarioResult = new ScenarioResult();

    private final ScenarioArguments scenarioArguments;
    private final ApplicationContext ctx;
    private final InterpreterDependencies dependencies;
    private final boolean stopScenarioOnFailure;
    private final CommandToInterpreterMap cmdToInterpreterMap;
    private final ResultUtil resultUtil;
    private final MobileUtil mobileUtil;
    private final BrowserUtil browserUtil;
    private final MobileBrowserDriverFactory mobileBrowserDriverFactory;
    private final WebDriverFactory webDriverFactory;
    private final NativeDriverFactory nativeDriverFactory;
    private final InjectionUtil injectionUtil;
    private final LogUtil logUtil;

    public ScenarioRunner(final ScenarioArguments scenarioArguments,
                          final ApplicationContext ctx) {
        this.scenarioArguments = scenarioArguments;
        this.ctx = ctx;
        this.resultUtil = ctx.getBean(ResultUtil.class);
        this.mobileUtil = ctx.getBean(MobileUtil.class);
        this.browserUtil = ctx.getBean(BrowserUtil.class);
        this.mobileBrowserDriverFactory = ctx.getBean(MobileBrowserDriverFactory.class);
        this.webDriverFactory = ctx.getBean(WebDriverFactory.class);
        this.nativeDriverFactory = ctx.getBean(NativeDriverFactory.class);
        this.injectionUtil = ctx.getBean(InjectionUtil.class);
        this.logUtil = ctx.getBean(LogUtil.class);
        this.stopScenarioOnFailure = ctx.getBean(GlobalTestConfiguration.class).isStopScenarioOnFailure();

        this.dependencies = createDependencies();
        this.cmdToInterpreterMap = createClassToInterpreterMap(dependencies);
    }

    public ScenarioResult run() {
        injectOverview();
        prepareScenarioResult();
        runScenarioCommands();
        return scenarioResult;
    }

    private void injectOverview() {
        Scenario scenario = scenarioArguments.getScenario();
        scenario.setOverview(injectionUtil.injectObject(scenario.getOverview(), dependencies.getScenarioContext()));
    }

    private void prepareScenarioResult() {
        Scenario scenario = scenarioArguments.getScenario();
        scenarioResult.setId(SCENARIO_ID_GENERATOR.incrementAndGet());
        scenarioResult.setOverview(scenario.getOverview());
        scenarioResult.setName(scenario.getOverview().getName());
        scenarioResult.setTags(scenario.getSettings().getTags());
        scenarioResult.setPath(scenarioArguments.getFile().getPath());
        scenarioResult.setBrowser(scenarioArguments.getBrowser());
        scenarioResult.setMobilebrowserDevice(scenarioArguments.getMobileBrowserDevice());
        scenarioResult.setNativeDevice(scenarioArguments.getNativeDevice());
        scenarioResult.setSuccess(true);
        scenarioResult.setEnvironment(scenarioArguments.getEnvironment());
    }

    private void runScenarioCommands() {
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
            long execTime = stopWatch.getDuration().toMillis();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            logUtil.logExecutionTime(execTime, command);
        }
        callback.onCommandExecuted(result);
    }

    private void executeCommand(final AbstractCommand command, final CommandResult result) {
        try {
            getInterpreterOrThrow(command).apply(command, result);
        } catch (Exception e) {
            resultUtil.setExceptionResult(result, e);
        }
    }

    private CommandResult prepareCommandResult(final AbstractCommand command) {
        return resultUtil.newCommandResultInstance(idGenerator.incrementAndGet(), command);
    }

    @SuppressWarnings("unchecked")
    private AbstractInterpreter<AbstractCommand> getInterpreterOrThrow(final AbstractCommand command) {
        AbstractInterpreter<? extends AbstractCommand> interpreter = cmdToInterpreterMap.get(command.getClass());
        if (interpreter == null) {
            throw new DefaultFrameworkException(FUNCTION_FOR_COMMAND_NOT_FOUND, command.getClass());
        }
        return (AbstractInterpreter<AbstractCommand>) interpreter;
    }

    private void handleException(final CommandResult result) {
        Exception ex = result.getException();
        if (ex != null) {
            logUtil.logException(ex);
            fillReportException(ex);
            if (stopScenarioOnFailure) {
                throw new StopSignalException();
            }
        }
    }

    private void fillReportException(final Exception ex) {
        if (StringUtils.isBlank(scenarioResult.getCause())) {
            String cause = StringUtils.isBlank(ex.getMessage()) ? ex.getClass().getSimpleName() : ex.getMessage();
            scenarioResult.setCause(cause);
            scenarioResult.setSuccess(false);
        }
    }

    private CommandToInterpreterMap createClassToInterpreterMap(final InterpreterDependencies dependencies) {
        CommandToInterpreterMap interpreterMap = new CommandToInterpreterMap();
        InterpreterScanner.getInterpreters().forEach((key, value) -> {
            try {
                interpreterMap.put(key, createInterpreterInstance(dependencies, value));
            } catch (Exception e) {
                throw new DefaultFrameworkException(MISSING_CONSTRUCTOR, value);
            }
        });
        return interpreterMap;
    }

    @SneakyThrows
    private AbstractInterpreter<? extends AbstractCommand> createInterpreterInstance(
            final InterpreterDependencies dependencies,
            final Class<AbstractInterpreter<? extends AbstractCommand>> value) {
        Constructor<AbstractInterpreter<? extends AbstractCommand>> constructor =
                value.getConstructor(InterpreterDependencies.class);
        AbstractInterpreter<? extends AbstractCommand> instance = constructor.newInstance(dependencies);
        ctx.getAutowireCapableBeanFactory().autowireBean(instance);
        return instance;
    }

    private InterpreterDependencies createDependencies() {
        return InterpreterDependencies.builder()
                .context(ctx)
                .file(scenarioArguments.getFile())
                .scenarioContext(new ScenarioContext(scenarioArguments.getVariations()))
                .position(idGenerator)
                .environment(scenarioArguments.getEnvironment())
                .webDriver(createWebDriver())
                .mobilebrowserDriver(createMobileBrowserDriver())
                .nativeDriver(createNativeDriver())
                .build();
    }

    private WebDriver createWebDriver() {
        return browserUtil.getBrowserBy(scenarioArguments.getEnvironment(), scenarioArguments.getBrowser())
                .map(webDriverFactory::createDriver)
                .orElse(new MockDriver(WEB_DRIVER_NOT_INIT));
    }

    private WebDriver createMobileBrowserDriver() {
        return mobileUtil.getMobileBrowserDeviceBy(scenarioArguments.getEnvironment(),
                        scenarioArguments.getMobileBrowserDevice())
                .map(mobileBrowserDriverFactory::createDriver)
                .orElse(new MockDriver(MOBILEBROWSER_DRIVER_NOT_INIT));
    }

    private WebDriver createNativeDriver() {
        return mobileUtil.getNativeDeviceBy(scenarioArguments.getEnvironment(), scenarioArguments.getNativeDevice())
                .map(nativeDriverFactory::createDriver)
                .orElse(new MockDriver(NATIVE_DRIVER_NOT_INIT));
    }

    public interface CommandCallback {
        void onCommandExecuted(CommandResult result);
    }
}
