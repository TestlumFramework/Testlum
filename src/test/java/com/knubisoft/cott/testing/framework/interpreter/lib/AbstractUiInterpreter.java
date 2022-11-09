package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.ScenarioUtil;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.Ui;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.WebStorage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_COOKIES_AFTER_EXECUTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_LOCAL_STORAGE_BY_KEY;

public abstract class AbstractUiInterpreter<T extends Ui> extends AbstractInterpreter<T> {

    protected AbstractUiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    public ExecutorDependencies createExecutorDependencies(final UiType uiType) {
        return ExecutorDependencies.builder()
                .file(dependencies.getFile())
                .driver(uiType.getAppropriateDriver(dependencies))
                .scenarioContext(dependencies.getScenarioContext())
                .position(dependencies.getPosition())
                .takeScreenshots(uiType.isScreenshotsEnabled(dependencies.getGlobalTestConfiguration()))
                .build();
    }

    public void runCommands(final List<AbstractUiCommand> commandList, final CommandResult result,
                            final ExecutorDependencies dependencies) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractUiCommand uiCommand : commandList) {
            LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), uiCommand);
            processEachCommand(uiCommand, subCommandsResult, dependencies);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand command,
                                    final List<CommandResult> subCommandsResult,
                                    final ExecutorDependencies dependencies) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        executeUiCommand(command, subCommandResult, dependencies);
        subCommandsResult.add(subCommandResult);
    }

    private void executeUiCommand(final AbstractUiCommand command,
                                  final CommandResult subCommandResult,
                                  final ExecutorDependencies dependencies) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            ExecutorProvider.getAppropriateExecutor(command, dependencies).execute(command, subCommandResult);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(subCommandResult, e);
            LogUtil.logException(e);
            ScenarioUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            subCommandResult.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
    }

    public void clearLocalStorage(final WebDriver driver, final String key, final CommandResult result) {
        if (StringUtils.isNotEmpty(key)) {
            result.put(CLEAR_LOCAL_STORAGE_BY_KEY, key);
            WebStorage webStorage = (WebStorage) driver;
            webStorage.getLocalStorage().removeItem(key);
        }
    }

    public void clearCookies(final WebDriver driver, final boolean clearCookies, final CommandResult result) {
        result.put(CLEAR_COOKIES_AFTER_EXECUTION, clearCookies);
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }

    public enum UiType {
        WEB(globalTestConfiguration ->
                globalTestConfiguration.getWeb().getBrowserSettings().getTakeScreenshots().isEnable(),
                InterpreterDependencies::getWebDriver),
        NATIVE(globalTestConfiguration ->
                globalTestConfiguration.getNative().getDeviceSettings().getTakeScreenshots().isEnable(),
                InterpreterDependencies::getNativeDriver),
        MOBILE_BROWSER(globalTestConfiguration ->
                globalTestConfiguration.getMobilebrowser().getDeviceSettings().getTakeScreenshots().isEnable(),
                InterpreterDependencies::getMobilebrowserDriver);

        private final Function<GlobalTestConfiguration, Boolean> screenshotFunction;
        private final Function<InterpreterDependencies, WebDriver> driverFunction;

        UiType(final Function<GlobalTestConfiguration, Boolean> screenshotFunction,
               final Function<InterpreterDependencies, WebDriver> driverFunction) {
            this.screenshotFunction = screenshotFunction;
            this.driverFunction = driverFunction;
        }

        public boolean isScreenshotsEnabled(final GlobalTestConfiguration globalTestConfiguration) {
            return screenshotFunction.apply(globalTestConfiguration);
        }

        public WebDriver getAppropriateDriver(final InterpreterDependencies interpreterDependencies) {
            return driverFunction.apply(interpreterDependencies);
        }
    }
}
