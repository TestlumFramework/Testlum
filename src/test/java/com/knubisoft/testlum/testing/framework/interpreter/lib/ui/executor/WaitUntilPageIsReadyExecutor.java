package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import com.knubisoft.testlum.testing.model.scenario.WaitUntilPageIsReady;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.PAGE_NOT_LOADED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static java.lang.Long.parseLong;

@Slf4j
@ExecutorForClass(WaitUntilPageIsReady.class)
public class WaitUntilPageIsReadyExecutor extends AbstractUiExecutor<WaitUntilPageIsReady> {

    private static final int TO_MILLIS = 1000;
    private static final int INTERVAL = 1000;

    public WaitUntilPageIsReadyExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void execute(final WaitUntilPageIsReady o, final CommandResult result) {
        log.info(WAIT_INFO_LOG, o.getTime(), o.getUnit());
        if (!executeAndWaitUntilPageIsReady(o)) {
            throw new DefaultFrameworkException(PAGE_NOT_LOADED);
        }
    }

    @SneakyThrows
    private Boolean executeAndWaitUntilPageIsReady(final WaitUntilPageIsReady o) {
        JavascriptExecutor js = (JavascriptExecutor) dependencies.getDriver();
        long timeToWait = o.getUnit() == Timeunit.MILLIS ? parseLong(o.getTime()) : parseLong(o.getTime()) * TO_MILLIS;
        long spentTimeOnWaitBeforeReload = waitBeforeStartOfReload(timeToWait);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeToWait - spentTimeOnWaitBeforeReload) {
            String pageState = checkIfPageIsLoadedJS(js);
            if ("complete".equalsIgnoreCase(pageState)) {
                return true;
            }
            Thread.sleep(INTERVAL);
        }
        return false;
    }

    private long waitBeforeStartOfReload(final long timeToWait) {
        long startTime = System.currentTimeMillis();
        WebDriverWait wait = new WebDriverWait(dependencies.getDriver(), Duration.ofSeconds(timeToWait));
        wait.until(ExpectedConditions.stalenessOf(
                dependencies.getDriver().findElement(By.xpath("/html"))));
        return System.currentTimeMillis() - startTime;
    }

    private String checkIfPageIsLoadedJS(final JavascriptExecutor js) {
        return (String) js.executeScript("return document.readyState");
    }
}
