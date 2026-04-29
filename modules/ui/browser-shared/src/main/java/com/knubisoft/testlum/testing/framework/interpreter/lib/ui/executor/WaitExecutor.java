package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.UiWait;
import com.knubisoft.testlum.testing.model.scenario.Visible;
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
            if (Objects.nonNull(wait.getVisible())) {
                waitIfVisibleOrClickable(wait.getVisible(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else if (Objects.nonNull(wait.getClickable())) {
                waitIfVisibleOrClickable(wait.getClickable(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else {
                waitUtil.sleep(Long.parseLong(time), timeUnit);
            }
        } catch (Exception e) {
            logUtil.logException(e);
            resultUtil.setExceptionResult(result, e);
            configUtil.checkIfStopScenarioOnFailure(e);
        }
    }

    private void waitIfVisibleOrClickable(final CommandWithLocator command,
                                          final Long seconds,
                                          final CommandResult result) {
        Duration duration = Duration.ofSeconds(seconds);
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), duration);
        WebElement element = uiUtil.findWebElement(dependencies, command.getLocator(), command.getLocatorStrategy());
        log.info(LogMessage.LOCATOR_LOG, command.getLocator());
        result.put(ResultUtil.LOCATOR_ID, command.getLocator());
        if (command instanceof Visible) {
            wait.until(ExpectedConditions.visibilityOf(element));
            log.info(LogMessage.WAIT_TYPE, "Visible");
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            log.info(LogMessage.WAIT_TYPE, "Clickable");
        }
    }
}
