package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.pages.Locator;
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

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEB_ELEMENT_ATTRIBUTE_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.HIGHLIGHT_SCRIPT;
import static java.lang.String.format;
import static org.openqa.selenium.interactions.PointerInput.Origin.viewport;

@Slf4j
@UtilityClass
public class UiUtil {

    private static final int MAX_PERCENTS_VALUE = 100;

    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getWeb().getBrowserSettings().getElementAutowait().getSeconds();

    private static final String FILE_PATH_PREFIX = "file:";

    public String resolveSendKeysType(final String value, final WebElement element, final File fromDir) {
        if (value.startsWith(FILE_PATH_PREFIX)) {
            File file = FileSearcher.searchFileFromDir(fromDir, value.substring(FILE_PATH_PREFIX.length()));
            return file.getPath();
        }
        element.clear();
        return value;
    }

    public WebElement findWebElement(final WebDriver webDriver, final String locatorId) {
        Locator locator = GlobalLocators.getLocator(locatorId);
        return WebElementFinder.find(locator, webDriver);
    }

    public void highlightElementIfRequired(final Boolean isHighlight,
                                           final WebElement element,
                                           final WebDriver driver) {
        if ((isHighlight == null || isHighlight) && !(driver instanceof AppiumDriver)) {
            JavascriptUtil.executeJsScript(element, HIGHLIGHT_SCRIPT, driver);
        }
    }

    public void waitForElementVisibility(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForElementToBeClickable(final WebDriver driver, final WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIME_TO_WAIT));
        highlightElementIfRequired(true, element, driver);
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

    public void takeScreenshotAndSaveIfRequired(final CommandResult result, final ExecutorDependencies dependencies) {
        if (dependencies.isTakeScreenshots()) {
            File screenshot = takeScreenshot(dependencies.getDriver());
            File screenshotsFolder = new File(dependencies.getFile().getParent()
                    + TestResourceSettings.SCREENSHOT_FOLDER);
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

    public String getElementAttribute(final WebElement webElement, final String attributeName) {
        String attribute = webElement.getAttribute(attributeName);
        if (Objects.isNull(attribute)) {
            throw new DefaultFrameworkException(WEB_ELEMENT_ATTRIBUTE_NOT_EXIST, attributeName);
        }
        return attribute;
    }

    public float calculatePercentageValue(final String value) {
        float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
        if (percent > 1) {
            throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
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
        return new Point(dimension.height / 2, dimension.height / 2);
    }
}
