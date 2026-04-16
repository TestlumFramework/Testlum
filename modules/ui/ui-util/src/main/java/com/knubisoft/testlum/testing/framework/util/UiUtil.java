package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.locator.LocatorProvider;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import io.appium.java_client.AppiumDriver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.regex.Pattern;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.JavascriptConstant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UiUtil implements ScreenshotHandler {

    private static final String FILE_PATH_PREFIX = "file:";
    private static final String APPIUM_LOCALHOST_ALIAS = "10.0.2.2";
    private static final String LOCALHOST = "localhost";
    private static final int MAX_PERCENTS_VALUE = 100;
    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    private final LocatorProvider locatorProvider;
    private final JavascriptUtil javascriptUtil;
    private final WebElementFinder webElementFinder;
    private final FileSearcher fileSearcher;
    private final EnvironmentLoader environmentLoader;
    private final ImageCompressor imageCompressor;
    private final ConfigProvider configProvider;

    public String resolveSendKeysType(final String value, final WebElement element, final File fromDir) {
        if (value.startsWith(FILE_PATH_PREFIX)) {
            File file = fileSearcher.searchFileFromDir(fromDir, value.substring(FILE_PATH_PREFIX.length()));
            return file.getPath();
        }
        element.clear();
        return value;
    }

    public WebElement findWebElement(final ExecutorDependencies dependencies,
                                     final String locatorId,
                                     final LocatorStrategy locatorStrategy) {
        Locator locator = getLocatorByStrategy(locatorId, locatorStrategy);
        return webElementFinder.find(locator, dependencies);
    }

    public Locator getLocatorByStrategy(final String locatorId, final LocatorStrategy locatorStrategy) {
        if (locatorStrategy == LocatorStrategy.LOCATOR_ID) {
            return locatorProvider.getLocator(locatorId);
        }
        Locator locator = new Locator();
        locator.setLocatorId(locatorId);
        addLocatorElement(locator, locatorId, locatorStrategy);
        return locator;
    }

    private void addLocatorElement(final Locator locator, final String locatorId,
                                   final LocatorStrategy strategy) {
        switch (strategy) {
            case XPATH -> locator.getXpathOrIdOrClassName().add(createXpath(locatorId));
            case ID -> locator.getXpathOrIdOrClassName().add(createId(locatorId));
            case TEXT -> locator.getXpathOrIdOrClassName().add(createText(locatorId));
            case CLASS -> locator.getXpathOrIdOrClassName().add(createClassName(locatorId));
            case CSS_SELECTOR -> locator.getXpathOrIdOrClassName().add(createCssSelector(locatorId));
            default -> { }
        }
    }

    private Xpath createXpath(final String value) {
        Xpath xpath = new Xpath();
        xpath.setValue(value);
        return xpath;
    }

    private Id createId(final String value) {
        Id id = new Id();
        id.setValue(value);
        return id;
    }

    private Text createText(final String value) {
        Text text = new Text();
        text.setPlaceholder(false);
        text.setValue(value);
        return text;
    }

    private ClassName createClassName(final String value) {
        ClassName className = new ClassName();
        className.setValue(value);
        return className;
    }

    private CssSelector createCssSelector(final String value) {
        CssSelector cssSelector = new CssSelector();
        cssSelector.setValue(value);
        return cssSelector;
    }


    public void highlightElementIfRequired(final boolean isHighlight,
                                           final WebElement element,
                                           final WebDriver driver) {
        if (isHighlight && !(driver instanceof AppiumDriver)) {
            javascriptUtil.executeJsScript(JavascriptConstant.HIGHLIGHT_SCRIPT, driver, element);
        }
    }

    public void waitForElementVisibility(final ExecutorDependencies dependencies, final WebElement element) {
        WebDriverWait wait = getWebDriverWait(dependencies);
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementToBeClickable(final ExecutorDependencies dependencies, final WebElement element) {
        WebDriverWait wait = getWebDriverWait(dependencies);
        highlightElementIfRequired(true, element, dependencies.getDriver());
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForElementToBeClickableNoHighlight(final ExecutorDependencies dependencies,
                                                       final WebElement element) {
        WebDriverWait wait = getWebDriverWait(dependencies);
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    public void waitForTextToBePresentInElement(final ExecutorDependencies dependencies,
                                                final WebElement element,
                                                final String text) {
        WebDriverWait wait = getWebDriverWait(dependencies);
        wait.until(ExpectedConditions.textToBePresentInElement(element, text));
    }

    public void waitForElementToBeSelected(final ExecutorDependencies dependencies, final WebElement element) {
        WebDriverWait wait = getWebDriverWait(dependencies);
        wait.until(ExpectedConditions.elementToBeSelected(element));
    }

    private WebDriverWait getWebDriverWait(final ExecutorDependencies dependencies) {
        int secondsToWait = dependencies.getUiType().getSettings(dependencies.getEnvironment(), configProvider)
                .getElementAutowait().getSeconds();
        return new WebDriverWait(dependencies.getDriver(), Duration.ofSeconds(secondsToWait));
    }

    public void takeScreenshotAndSaveIfRequired(final CommandResult result, final ExecutorDependencies dependencies) {
        boolean isTakeScreenshots = dependencies.getUiType().getSettings(dependencies.getEnvironment(), configProvider)
                .getTakeScreenshots().isEnabled();
        if (isTakeScreenshots) {
            File screenshot = takeScreenshot(dependencies.getDriver());
            File screenshotsFolder = new File(dependencies.getFile().getParent(),
                    TestResourceSettings.SCREENSHOT_FOLDER);
            tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
            putScreenshotToResult(result, screenshot);
        }
    }

    private void tryToCopyScreenshotFileToFolder(final File screenshot,
                                                 final File screenshotsFolder,
                                                 final ExecutorDependencies dependencies) {
        try {
            copyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }


    private void copyScreenshotFileToFolder(final File screenshot,
                                            final File screenshotsFolder,
                                            final ExecutorDependencies dependencies) throws IOException {
        LocalTime dateTime = LocalTime.now();
        String screenshotFileName = String.format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                dateTime.format(TIME_FORMATTER),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }

    public File takeScreenshot(final WebDriver webDriver) {
        return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
    }

    public File takeScreenshot(final WebElement webElement) {
        return webElement.getScreenshotAs(OutputType.FILE);
    }

    public void putScreenshotToResult(final CommandResult result, final File screenshot) {
        byte[] compressedImage = imageCompressor.compress(screenshot);
        String encodedScreenshot = Base64.getEncoder().encodeToString(compressedImage);
        result.setBase64Screenshot(encodedScreenshot);
    }

    public String getElementAttribute(final WebElement element, final String attributeName, final WebDriver driver) {
        String attribute = (String) javascriptUtil.executeJsScript(
                JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT, driver, element, attributeName);
        attribute = StringUtils.isBlank(attribute) ? element.getAttribute(attributeName) : attribute;
        if (StringUtils.isBlank(attribute)) {
            throw new DefaultFrameworkException(ExceptionMessage.WEB_ELEMENT_ATTRIBUTE_NOT_EXIST, attributeName);
        }
        return attribute;
    }

    public String resolveHostIfNeeded(final String url) {
        return url.replaceAll(APPIUM_LOCALHOST_ALIAS, LOCALHOST);
    }

    public float calculatePercentageValue(final float value) {
        float percent = value / MAX_PERCENTS_VALUE;
        if (percent > 1) {
            throw new DefaultFrameworkException(ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED, value);
        }
        return percent;
    }

    public Sequence buildSequence(final Point start, final Point end, final int duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0),
                        PointerInput.Origin.viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(duration),
                        PointerInput.Origin.viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    }

    public Point getCenterPoint(final WebDriver driver) {
        Dimension dimension = driver.manage().window().getSize();
        return new Point(dimension.width / 2, dimension.height / 2);
    }

    public String getUrl(final String path, final String env, final UiType uiType) {
        if (HTTP_PATTERN.matcher(path).matches()) {
            return path;
        }
        if (UiType.MOBILE_BROWSER == uiType) {
            return environmentLoader.getMobileBrowserSettings(env).get().getBaseUrl() + path;
        }
        return environmentLoader.getWebSettings(env).get().getBaseUrl() + path;
    }

    public String getBasePageURL(final String currentPageURL) {
        try {
            URL url = new URL(currentPageURL);
            String protocol = url.getProtocol();
            String host = url.getHost();

            return String.format("%s://%s", protocol, host);
        } catch (Exception e) {
            throw new DefaultFrameworkException(
                    String.format("Unable to extract base URL from page: %s", currentPageURL));
        }
    }
}
