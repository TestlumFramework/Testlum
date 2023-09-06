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
import org.openqa.selenium.JavascriptExecutor;

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
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeToWait) {
            String pageState = checkIfPageIsLoadedJS(js);
            if ("complete".equalsIgnoreCase(pageState)) {
                return true;
            }
            Thread.sleep(INTERVAL);
        }
        return false;
    }

    private String checkIfPageIsLoadedJS(final JavascriptExecutor js) {
        return (String) js.executeScript("return document.readyState");
    }
}
