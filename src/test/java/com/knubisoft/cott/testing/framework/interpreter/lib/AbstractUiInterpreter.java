package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.ScenarioUtil;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.SwitchToFrame;
import com.knubisoft.cott.testing.model.scenario.Ui;
import com.knubisoft.cott.testing.model.scenario.WebView;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.html5.WebStorage;

import java.util.LinkedList;
import java.util.List;

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
                .takeScreenshots(uiType.isScreenshotsEnabled())
                .uiType(uiType)
                .build();
    }

    public void runCommands(final List<AbstractUiCommand> commandList,
                            final CommandResult result,
                            final ExecutorDependencies dependencies) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractUiCommand uiCommand : commandList) {
            LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), uiCommand);
            processEachCommand(uiCommand, result, dependencies);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand uiCommand,
                                    final CommandResult result,
                                    final ExecutorDependencies dependencies) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                uiCommand.getClass().getSimpleName(),
                uiCommand.getComment());
        executeUiCommand(uiCommand, subCommandResult, dependencies);
        result.getSubCommandsResult().add(subCommandResult);
        processIfSwitchToFrame(uiCommand, subCommandResult, dependencies);
        processIfWebView(uiCommand, subCommandResult, dependencies);
    }

    private void executeUiCommand(final AbstractUiCommand uiCommand,
                                  final CommandResult subCommandResult,
                                  final ExecutorDependencies dependencies) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            getAppropriateExecutor(uiCommand, dependencies).apply(uiCommand, subCommandResult);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(subCommandResult, e);
            LogUtil.logException(e);
            ScenarioUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            subCommandResult.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, uiCommand);
        }
    }

    private AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand uiCommand,
                                                                         final ExecutorDependencies dependencies) {
        AbstractUiExecutor<AbstractUiCommand> executor
                = ExecutorProvider.getAppropriateExecutor(uiCommand, dependencies);
        this.dependencies.getCxt().getAutowireCapableBeanFactory().autowireBean(executor);
        return executor;
    }

    private void processIfSwitchToFrame(final AbstractUiCommand uiCommand,
                                        final CommandResult result,
                                        final ExecutorDependencies dependencies) {
        if (uiCommand instanceof SwitchToFrame) {
            LogUtil.startUiCommandsInFrame();
            runCommands(((SwitchToFrame) uiCommand).getClickOrInputOrAssert(), result, dependencies);
            LogUtil.endUiCommandsInFrame();
            dependencies.getDriver().switchTo().defaultContent();
        }
    }

    private void processIfWebView(final AbstractUiCommand uiCommand,
                                  final CommandResult result,
                                  final ExecutorDependencies dependencies) {
        if (uiCommand instanceof WebView) {
            LogUtil.startUiCommandsInWebView();
            runCommands(((WebView) uiCommand).getClickOrInputOrAssert(), result, dependencies);
            ((SupportsContextSwitching) dependencies.getDriver()).context("NATIVE_APP");
            LogUtil.endUiCommandsInWebView();
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
}
