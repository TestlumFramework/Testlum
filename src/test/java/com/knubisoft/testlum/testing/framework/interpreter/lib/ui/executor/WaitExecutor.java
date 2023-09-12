package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
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
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_TYPE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_ID;
import static java.util.Objects.nonNull;

@ExecutorForClass(UiWait.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<UiWait> {

    private final WaitUtil waitUtil;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.waitUtil = dependencies.getContext().getBean(WaitUtil.class);
    }

    @Override
    public void execute(final UiWait uiWait, final CommandResult result) {
        String time = uiWait.getTime();
        log.info(WAIT_INFO_LOG, time, uiWait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(uiWait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        wait(uiWait, time, timeUnit, result);
    }

    private void wait(final UiWait wait, final String time, final TimeUnit timeUnit, final CommandResult result) {
        try {
            if (nonNull(wait.getVisible())) {
                waitIfVisibleOrClickable(wait.getVisible(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else if (nonNull(wait.getClickable())) {
                waitIfVisibleOrClickable(wait.getClickable(), timeUnit.toSeconds(Long.parseLong(time)), result);
            } else {
                waitUtil.sleep(Long.parseLong(time), timeUnit);
            }
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            checkIfStopScenarioOnFailure(e);
        }
    }

    private void waitIfVisibleOrClickable(final CommandWithLocator command,
                                          final Long seconds,
                                          final CommandResult result) {
        Duration duration = Duration.ofSeconds(seconds);
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), duration);
        WebElement element = uiUtil.findWebElement(dependencies, command.getLocatorId());
        log.info(LOCATOR_LOG, command.getLocatorId());
        result.put(LOCATOR_ID, command.getLocatorId());
        if (command instanceof Visible) {
            wait.until(ExpectedConditions.visibilityOf(element));
            log.info(WAIT_TYPE, "Visible");
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            log.info(WAIT_TYPE, "Clickable");
        }
    }
}
