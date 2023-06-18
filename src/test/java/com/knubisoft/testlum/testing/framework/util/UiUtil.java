package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.locator.GlobalLocators;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.pages.Locator;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.WEB_ELEMENT_ATTRIBUTE_NOT_EXIST;
import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT;
import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.HIGHLIGHT_SCRIPT;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Slf4j
@UtilityClass
public class UiUtil {

    private static final String FILE_PATH_PREFIX = "file:";
    private static final String APPIUM_LOCALHOST_ALIAS = "10.0.2.2";
    private static final String LOCALHOST = "localhost";
    private static final int MAX_PERCENTS_VALUE = 100;

    public String resolveSendKeysType(final String value, final WebElement element, final File fromDir) {
        if (value.startsWith(FILE_PATH_PREFIX)) {
            File file = FileSearcher.searchFileFromDir(fromDir, value.substring(FILE_PATH_PREFIX.length()));
            return file.getPath();
        }
        element.clear();
        return value;
    }

    public WebElement findWebElement(final ExecutorDependencies dependencies, final String locatorId) {
        Locator locator = GlobalLocators.getLocator(locatorId);
        return WebElementFinder.find(locator, dependencies.getDriver());
    }

    public void highlightElementIfRequired(final boolean isHighlight,
                                           final WebElement element,
                                           final WebDriver driver) {
        if (isHighlight && !(driver instanceof AppiumDriver)) {
            JavascriptUtil.executeJsScript(HIGHLIGHT_SCRIPT, driver, element);
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
        int secondsToWait = dependencies.getUiType().getSettings(dependencies.getEnvironment())
                .getElementAutowait().getSeconds();
        return new WebDriverWait(dependencies.getDriver(), Duration.ofSeconds(secondsToWait));
    }

    public void takeScreenshotAndSaveIfRequired(final CommandResult result, final ExecutorDependencies dependencies) {
        boolean isTakeScreenshots = dependencies.getUiType().getSettings(dependencies.getEnvironment())
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
        String screenshotFileName = format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                LocalTime.now(),
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

    @SneakyThrows
    public void putScreenshotToResult(final CommandResult result, final File screenshot) {
        final MultipartFile image = ImageCompressor.compress(screenshot);
        if (Objects.nonNull(image)) {
            byte[] screenshotContent = FileUtils.readFileToByteArray(screenshot);
            String encodedScreenshot = Base64.getEncoder().encodeToString(screenshotContent);
            result.setBase64Screenshot(encodedScreenshot);
        }
    }

    public String getElementAttribute(final WebElement element, final String attributeName, final WebDriver driver) {
        String attribute = (String) JavascriptUtil.executeJsScript(
                ELEMENT_ARGUMENTS_SCRIPT, driver, element, attributeName);
        attribute = isBlank(attribute) ? element.getAttribute(attributeName) : attribute;
        if (isBlank(attribute)) {
            throw new DefaultFrameworkException(WEB_ELEMENT_ATTRIBUTE_NOT_EXIST, attributeName);
        }
        return attribute;
    }

    public String resolveHostIfNeeded(final String url) {
        return url.replaceAll(APPIUM_LOCALHOST_ALIAS, LOCALHOST);
    }

    public float calculatePercentageValue(final float value) {
        float percent = value / MAX_PERCENTS_VALUE;
        if (percent > 1) {
            throw new DefaultFrameworkException(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value);
        }
        return percent;
    }

    public Sequence buildSequence(final Point start, final Point end, final int duration) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        return new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ofMillis(0), viewport(), start.getX(), start.getY()))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(duration), viewport(), end.getX(), end.getY()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    }

    public Point getCenterPoint(final WebDriver driver) {
        Dimension dimension = driver.manage().window().getSize();
        return new Point(dimension.width / 2, dimension.height / 2);
    }
}
