package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;

import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.NOT_ENABLED_BROWSERS;

@UtilityClass
public class BrowserUtil {

    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getUi().getBrowserSettings().getWebElementAutowait().getSeconds();

    public List<AbstractBrowser> filterEnabledBrowsers() {
        List<AbstractBrowser> filteredResult = GlobalTestConfigurationProvider.getBrowsers().stream()
                .filter(AbstractBrowser::isEnable)
                .collect(Collectors.toList());
        if (filteredResult.isEmpty()) {
            throw new DefaultFrameworkException(NOT_ENABLED_BROWSERS);
        }
        return filteredResult;
    }

    public void waitForElementVisibility(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void manageWindowSize(final AbstractBrowser browser, final WebDriver webDriver) {
        String browserWindowSize = browser.getBrowserWindowSize();
        if (StringUtils.isNotEmpty(browser.getBrowserWindowSize())) {
            String[] size = browserWindowSize.split(DelimiterConstant.X);
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            webDriver.manage().window().setSize(new Dimension(width, height));
        }
        if (browser.isMaximizedBrowserWindow()) {
            webDriver.manage().window().maximize();
        }
    }
    public void waitForElementToBeClickable(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        ElementHighlighter.highlight(element, driver);
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForElementToBeClickableNoHighlight(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForTextToBePresentInElement(final WebDriver driver, final WebElement element, final String text) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public void waitForElementToBeSelected(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.elementToBeSelected(element));
    }

    //TODO add version and browser type
    public String getBrowserInfo(final AbstractBrowser browser) {
        return browser.getClass().getSimpleName();
    }
}
