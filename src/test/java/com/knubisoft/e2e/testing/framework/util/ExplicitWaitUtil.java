package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@UtilityClass
public final class ExplicitWaitUtil {
    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getUi().getWebDriverSettings().getWebElementAutowait().getSeconds();

    public void waitForElementVisibility(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_TO_WAIT);
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementToBeClickable(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_TO_WAIT);
        ElementHighlighter.highlight(element, driver);
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForElementToBeClickableNoHighlight(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_TO_WAIT);
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForTextToBePresentInElement(final WebDriver driver, final WebElement element, final String text) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_TO_WAIT);
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public void waitForElementToBeSelected(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, TIME_TO_WAIT);
        wait.until(ExpectedConditions.elementToBeSelected(element));
    }
}
