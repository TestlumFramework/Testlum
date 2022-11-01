package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorProvider;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.cott.testing.model.scenario.CompareWith;
import com.knubisoft.cott.testing.model.scenario.Image;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.WaitUi;
import io.appium.java_client.AppiumDriver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEB_ELEMENT_ATTRIBUTE_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.HIGHLIGHT_SCRIPT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_COOKIES_AFTER_EXECUTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_LOCAL_STORAGE_BY_KEY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.lang.String.format;

@Slf4j
@UtilityClass
public class UiUtil {

    private static final int MAX_PERCENTS_VALUE = 100;

    private static final int TIME_TO_WAIT = GlobalTestConfigurationProvider.provide()
            .getWeb().getBrowserSettings().getElementAutowait().getSeconds();

    private static final String FILE_PATH_PREFIX = "file:";

    public void runCommands(final List<AbstractUiCommand> commandList, final CommandResult result,
                               final ExecutorDependencies dependencies) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        for (AbstractUiCommand uiCommand : commandList) {
            LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), uiCommand);
            processEachCommand(uiCommand, subCommandsResult, dependencies);
        }
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractUiCommand command,
                                    final List<CommandResult> subCommandsResult,
                                    final ExecutorDependencies dependencies) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        executeUiCommand(command, subCommandResult, dependencies);
        subCommandsResult.add(subCommandResult);
    }

    //CHECKSTYLE:OFF
    private void executeUiCommand(final AbstractUiCommand command,
                                  final CommandResult subCommandResult,
                                  final ExecutorDependencies dependencies) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            ExecutorProvider.getAppropriateExecutor(command, dependencies).execute(command, subCommandResult);
            if (dependencies.isTakeScreenshots()
                    && !(command instanceof WaitUi || command instanceof Image || command instanceof Javascript)) {
                takeScreenshotAndSave(subCommandResult, dependencies);
            }
        } catch (Exception e) {
            ResultUtil.setExceptionResult(subCommandResult, e);
            LogUtil.logException(e);
            ScenarioUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            subCommandResult.setExecutionTime(execTime);
            LogUtil.logExecutionTime(execTime, command);
        }
    }
    //CHECKSTYLE:ON

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

    public void takeScreenshotAndSave(final CommandResult result, final ExecutorDependencies dependencies) {
        File screenshot = takeScreenshot(dependencies.getDriver());
        File screenshotsFolder = new File(dependencies.getFile().getParent()
                + TestResourceSettings.SCREENSHOT_FOLDER);
        tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
        putScreenshotToResult(result, screenshot);
    }

    private void tryToCopyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder,
                                                 final ExecutorDependencies dependencies) {
        try {
            copyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }


    private void copyScreenshotFileToFolder(final File screenshot,
                                            final File screenshotsFolder,
                                            final ExecutorDependencies dependencies)
            throws IOException {
        String screenshotFileName = format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                LocalTime.now(),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }

    private File takeScreenshot(final WebDriver webDriver) {
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

    public void clearLocalStorage(final WebDriver driver, final String key, final CommandResult result) {
        if (StringUtils.isNotEmpty(key)) {
            result.put(CLEAR_LOCAL_STORAGE_BY_KEY, key);
            WebStorage webStorage = (WebStorage) driver;
            webStorage.getLocalStorage().removeItem(key);
        }
    }

    public void clearCookies(final WebDriver driver, final boolean clearCookies, final CommandResult result) {
        result.put(CLEAR_COOKIES_AFTER_EXECUTION, clearCookies);
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }
}
