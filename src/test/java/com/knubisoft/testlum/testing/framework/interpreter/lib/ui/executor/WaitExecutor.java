package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Clickable;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.WaitFor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LOCATOR_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_ID;

@ExecutorForClass(WaitFor.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<WaitFor> {
    private static final int TIMEOUT_SECONDS = 10;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final WaitFor wait, final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        wait.getClickableOrVisible().forEach(action -> {
            int commandId = dependencies.getPosition().incrementAndGet();
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            LogUtil.logSubCommand(commandId, action);
            processEachAction(action, commandResult);
        });
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final CommandWithLocator command, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            executeWaitForCommand(command);
            log.info(LOCATOR_LOG, command.getLocatorId());
            result.put(LOCATOR_ID, command.getLocatorId());
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void executeWaitForCommand(final CommandWithLocator command) {
        Duration duration = Objects.nonNull(command.getThreshold())
                ? Duration.ofSeconds(command.getThreshold())
                : Duration.ofSeconds(TIMEOUT_SECONDS);
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), duration);
        WebElement element = UiUtil.findWebElement(dependencies, command.getLocatorId());
        if (command instanceof Clickable) {
            wait.until(ExpectedConditions.visibilityOf(element));
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(element));
        }
    }
}
