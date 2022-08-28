package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.ExtractFromElementAndCompare;
import com.knubisoft.cott.testing.model.scenario.ImageComparison;
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

import static com.knubisoft.cott.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;

@Slf4j
@UtilityClass
public class UiUtil {

    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getUi().getBrowserSettings().getWebElementAutowait().getSeconds();

    public WebElement findWebElement(final WebDriver webDriver, final String locatorId) {
        Locator locator = GlobalLocators.getLocator(locatorId);
        return WebElementFinder.find(locator, webDriver);
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
                                               final ImageComparison imageComparison,
                                               final CommandResult result) {
        ExtractFromElementAndCompare actualImageInfo = imageComparison.getExtractFromElementAndCompare();
        if (Objects.nonNull(actualImageInfo)) {
            WebElement webElement = findWebElement(webDriver, actualImageInfo.getLocatorId());
            return UiUtil.extractImageFromElement(webElement, actualImageInfo.getImageSourceAttributeName(), result);
        }
        return ImageIO.read(takeScreenshot(webDriver));
    }

    @SneakyThrows
    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) {
        String urlToActualImage = webElement.getAttribute(imageSourceAttribute);
        log.info(URL_TO_IMAGE_LOG, urlToActualImage);
        result.put(URL_TO_ACTUAL_IMAGE, urlToActualImage);
        return ImageIO.read(new URL(urlToActualImage));
    }
}
