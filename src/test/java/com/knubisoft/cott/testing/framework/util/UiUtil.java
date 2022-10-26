package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.CompareWith;
import com.knubisoft.cott.testing.model.scenario.Image;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEB_ELEMENT_ATTRIBUTE_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.HIGHLIGHT_SCRIPT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.lang.String.format;

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


    public File takeScreenshot(final WebDriver webDriver) {
        return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
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

    @SneakyThrows
    public BufferedImage getActualImage(final WebDriver webDriver,
                                        final Image image,
                                        final CommandResult result) {
        CompareWith compareWith = image.getCompareWith();
        if (Objects.nonNull(compareWith)) {
            WebElement webElement = findWebElement(webDriver, compareWith.getLocator());
            return UiUtil.extractImageFromElement(webElement, compareWith.getAttribute(), result);
        }
        return ImageIO.read(takeScreenshot(webDriver));
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

    @SneakyThrows
    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) {
        String urlToActualImage = getElementAttribute(webElement, imageSourceAttribute);
        log.info(URL_TO_IMAGE_LOG, urlToActualImage);
        result.put(URL_TO_ACTUAL_IMAGE, urlToActualImage);
        return ImageIO.read(new URL(urlToActualImage));
    }
}
