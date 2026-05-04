package com.knubisoft.testlum.testing.framework.util.check;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PageLoadCheck {

    private static final String DOM_COMPLETE = "complete";
    private static final long POLLING_INTERVAL_MS = 1000L;
    private static final int MAX_DOM_LOAD_WAIT_TIME_SECONDS = 30;

    public void waitUntilDomReady(final ExecutorDependencies executorDependencies) {
        if (executorDependencies.getUiType() == UiType.NATIVE) {
            return;
        }
        buildFluentWait(executorDependencies.getDriver())
                .withMessage("Time out is reached. Page is not loaded!")
                .until((ExpectedCondition<Boolean>) this::isDomComplete);
    }

    private FluentWait<WebDriver> buildFluentWait(final WebDriver driver) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(MAX_DOM_LOAD_WAIT_TIME_SECONDS))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
    }

    private boolean isDomComplete(final WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String domStatus = (String) Objects.requireNonNull(js).executeScript("return document.readyState");
        return DOM_COMPLETE.equals(domStatus);
    }

}
