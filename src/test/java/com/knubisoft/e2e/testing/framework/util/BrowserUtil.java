package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;

import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.BrowserVersion;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.BROWSER_INFO_TEMPLATE;
import static java.lang.String.format;

@UtilityClass
public class BrowserUtil {

    private static final String NOT_ENABLED_BROWSERS = "At least 1 browser must be enabled";
    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getUi().getBrowserSettings().getWebElementAutowait().getSeconds();

    public List<AbstractBrowser> filterEnabledBrowsers() {
        List<AbstractBrowser> filteredResult = GlobalTestConfigurationProvider.getBrowserVersions().stream()
                .filter(AbstractBrowser::isEnable)
                .collect(Collectors.toList());
        if (filteredResult.isEmpty()) {
            throw new DefaultFrameworkException(NOT_ENABLED_BROWSERS);
        }
        return filteredResult;
    }

    public boolean useLatest(final BrowserVersion browserVersion) {
        if (StringUtils.isBlank(browserVersion.getVersion()) && !browserVersion.isLatestVersion()) {
            return true;
        }
        return browserVersion.isLatestVersion();
    }

    public String getBrowserInfo(final AbstractBrowser browser) {
        BrowserVersion browserVersion = browser.getBrowserVersion();
        String version = useLatest(browserVersion) ? "latest" : browserVersion.getVersion();
        return format(BROWSER_INFO_TEMPLATE, browser.getClass().getSimpleName(), version);
    }

    public void waitForElementVisibility(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.visibilityOf(element));
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
}
