package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ConfigUtil;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.wait.util.WaitUtil;
import com.testlum.testing.model.scenario.CommandWithLocator;
import com.testlum.testing.model.scenario.UiWait;
import com.testlum.testing.model.scenario.Visible;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@ExecutorForClass(UiWait.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<UiWait> {

    private final WaitUtil waitUtil;
    private final ConfigUtil configUtil;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.waitUtil = dependencies.getContext().getBean(WaitUtil.class);
        this.configUtil = dependencies.getContext().getBean(ConfigUtil.class);
    }

    @Override
    public void execute(final UiWait uiWait, final CommandResult result) {
        String time = uiWait.getTime();
        log.info(LogMessage.WAIT_INFO_LOG, time, uiWait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(uiWait.getUnit());
        resultUtil.addWaitMetaData(time, timeUnit, result);
        wait(uiWait, time, timeUnit, result);
    }

    private void wait(final UiWait wait, final String time, final TimeUnit timeUnit, final CommandResult result) {
        try {
            CommandWithLocator command = getCommandWithLocator(wait);
            if (Objects.nonNull(command)) {
                waitByCommandWithLocator(command, time, timeUnit, result);
            } else {
                waitUtil.sleep(Long.parseLong(time), timeUnit);
            }
        } catch (Exception e) {
            logUtil.logException(e);
            resultUtil.setExceptionResult(result, e);
            configUtil.checkIfStopScenarioOnFailure(e);
        }
    }

    private CommandWithLocator getCommandWithLocator(final UiWait wait) {
        if (Objects.nonNull(wait.getVisible())) {
            return wait.getVisible();
        }
        return wait.getClickable();
    }

    private void waitByCommandWithLocator(final CommandWithLocator command,
                                          final String time,
                                          final TimeUnit timeUnit,
                                          final CommandResult result) {
        if (conditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), result)) {
            waitIfVisibleOrClickable(command, timeUnit.toSeconds(Long.parseLong(time)), result);
        }
    }

    private void waitIfVisibleOrClickable(final CommandWithLocator command,
                                          final Long seconds,
                                          final CommandResult r) {
        Duration duration = Duration.ofSeconds(seconds);
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), duration);
        WebElement element = uiUtil.findWebElement(dependencies, command.getLocator(), command.getLocatorStrategy(), r);
        log.info(LogMessage.LOCATOR_LOG, command.getLocator());
        r.put(ResultUtil.LOCATOR_ID, command.getLocator());
        if (command instanceof Visible) {
            wait.until(ExpectedConditions.visibilityOf(element));
            log.info(LogMessage.WAIT_TYPE, "Visible");
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            log.info(LogMessage.WAIT_TYPE, "Clickable");
        }
    }
}
