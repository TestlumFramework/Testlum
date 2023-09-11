package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import com.knubisoft.testlum.testing.model.scenario.UiWait;
import com.knubisoft.testlum.testing.model.scenario.Visible;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.PAGE_DID_NOT_RELOAD;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.PAGE_NOT_LOADED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.LOCATOR_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_TYPE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_ID;
import static java.lang.Long.parseLong;
import static java.util.Objects.nonNull;

@ExecutorForClass(UiWait.class)
@Slf4j
public class WaitExecutor extends AbstractUiExecutor<UiWait> {

    private static final int TO_MILLIS = 1000;
    private static final int INTERVAL = 1000;

    public WaitExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final UiWait uiWait, final CommandResult result) {
        String time = uiWait.getTime();
        log.info(WAIT_INFO_LOG, time, uiWait.getUnit());
        TimeUnit timeUnit = WaitUtil.getTimeUnit(uiWait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        wait(uiWait, time, timeUnit, result);
    }

    private void wait(final UiWait wait, final String time, final TimeUnit timeUnit, final CommandResult result) {
        try {
            executeAppropriateWait(wait, time, timeUnit, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        }
    }

    private void executeAppropriateWait(final UiWait wait,
                                        final String time,
                                        final TimeUnit timeUnit,
                                        final CommandResult result) {
        if (nonNull(wait.getVisible())) {
            waitIfVisibleOrClickable(wait.getVisible(), timeUnit.toSeconds(Long.parseLong(time)), result);
        } else if (nonNull(wait.getClickable())) {
            waitIfVisibleOrClickable(wait.getClickable(), timeUnit.toSeconds(Long.parseLong(time)), result);
        } else if (nonNull(wait.getPageLoad())) {
            waitIfPageLoad(wait.getPageLoad().getLocatorId(), time, wait.getUnit());
        } else {
            WaitUtil.sleep(Long.parseLong(time), timeUnit);
        }
    }

    private void waitIfVisibleOrClickable(final CommandWithLocator command,
                                          final Long seconds,
                                          final CommandResult result) {
        Duration duration = Duration.ofSeconds(seconds);
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), duration);
        WebElement element = UiUtil.findWebElement(dependencies, command.getLocatorId());
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

    private void waitIfPageLoad(final String locatorId, final String time, final Timeunit unit) {
        if (!executeAndWaitUntilPageIsReady(locatorId, time, unit)) {
            throw new DefaultFrameworkException(PAGE_NOT_LOADED);
        }
    }

    @SneakyThrows
    private boolean executeAndWaitUntilPageIsReady(final String locatorId, final String time, final Timeunit unit) {
        JavascriptExecutor js = (JavascriptExecutor) dependencies.getDriver();
        long timeToWait = unit == Timeunit.MILLIS ? parseLong(time) : parseLong(time) * TO_MILLIS;
        long spentTimeForReload = waitForReload(locatorId, timeToWait);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeToWait - spentTimeForReload) {
            String pageState = checkIfPageIsLoadedJS(js);
            if ("complete".equalsIgnoreCase(pageState)) {
                return true;
            }
            Thread.sleep(INTERVAL);
        }
        return false;
    }

    private long waitForReload(final String locatorId, final long timeToWait) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeToWait) {
            try {
                UiUtil.findWebElement(dependencies, locatorId);
                return System.currentTimeMillis() - startTime;
            } catch (NoSuchElementException ignored) {
                //ignored
            }
        }
        throw new DefaultFrameworkException(PAGE_DID_NOT_RELOAD);
    }

    private String checkIfPageIsLoadedJS(final JavascriptExecutor js) {
        return (String) js.executeScript("return document.readyState");
    }
}
