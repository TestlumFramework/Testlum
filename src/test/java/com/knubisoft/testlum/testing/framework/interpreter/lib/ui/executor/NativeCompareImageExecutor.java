package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.ImageComparator;
import com.knubisoft.testlum.testing.framework.util.ImageComparisonUtil;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Part;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IOS_NOT_SUPPORT_PART_COMMAND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NATIVE_NOT_SUPPORT_PICTURE_COMMAND;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(NativeImage.class)
public class NativeCompareImageExecutor extends AbstractUiExecutor<NativeImage> {

    private static final String MOBILE_SCREEN_HEIGHT = "return screen.height;";
    private static final String WINDOW_INNER_HEIGHT = "return window.innerHeight;";
    private static final String STATUS_BAR_HEIGHT = "statBarHeight";

    public NativeCompareImageExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @SneakyThrows
    @Override
    protected void execute(final NativeImage image, final CommandResult result) {
        LogUtil.logImageComparisonInfo(image);
        ResultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expected = ImageIO.read(FileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actual = getActualImage(dependencies.getDriver(), image, result);
        ImageComparisonResult comparisonResult =
                ImageComparator.compareNative(image, expected, cutStatusBar(actual, dependencies.getDriver()));
        ImageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                image.isHighlightDifference(), scenarioFile.getParentFile(), result);
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final NativeImage image,
                                         final CommandResult result) throws IOException {
        if (nonNull(image.getPicture())) {
            return getElementsPicture(image, result);
        }
        if (nonNull(image.getPart())) {
            return getElementAsScreenshot(webDriver, image.getPart());
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }

    private BufferedImage getElementsPicture(final NativeImage image, final CommandResult result) throws IOException {
        if (UiType.NATIVE == dependencies.getUiType()) {
            throw new DefaultFrameworkException(NATIVE_NOT_SUPPORT_PICTURE_COMMAND);
        }
        WebElement webElement = UiUtil.findWebElement(dependencies, image.getPicture().getLocatorId());
        return extractImageFromElement(webElement, image.getPicture().getAttribute(), result);
    }

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToImage = UiUtil.getElementAttribute(webElement, imageSourceAttribute, dependencies.getDriver());
        urlToImage = UiUtil.resolveHostIfNeeded(urlToImage);
        log.info(URL_TO_IMAGE_LOG, urlToImage);
        result.put(URL_TO_ACTUAL_IMAGE, urlToImage);
        return ImageIO.read(new URL(urlToImage));
    }

    private BufferedImage getElementAsScreenshot(final WebDriver webDriver,
                                                 final Part part) throws IOException {
        String platformName = ((RemoteWebDriver) webDriver).getCapabilities().getPlatformName().name();
        if (isIosMobilebrowser(platformName)) {
            throw new DefaultFrameworkException(IOS_NOT_SUPPORT_PART_COMMAND);
        }
        WebElement webElement = UiUtil.findWebElement(dependencies, part.getLocatorId());
        return ImageIO.read(webElement.getScreenshotAs(OutputType.FILE));
    }

    private BufferedImage cutStatusBar(final BufferedImage screenshot, final WebDriver driver) {
        Capabilities capabilities = ((RemoteWebDriver) driver).getCapabilities();
        if (isNativeOrIosMobilebrowser(capabilities)) {
            int statusBarHeight = getStatusBarHeight(driver);
            return screenshot.getSubimage(0, statusBarHeight, screenshot.getWidth(),
                    screenshot.getHeight() - statusBarHeight);
        }
        return screenshot;
    }

    private int getStatusBarHeight(final WebDriver driver) {
        Capabilities deviceCapabilities = ((RemoteWebDriver) dependencies.getDriver()).getCapabilities();
        if (isNativeOrIosMobilebrowser(deviceCapabilities)) {
            if (nonNull(deviceCapabilities.getCapability(STATUS_BAR_HEIGHT))) {
                return (int) ((long) deviceCapabilities.getCapability(STATUS_BAR_HEIGHT));
            }
            long screenHeight = (Long) JavascriptUtil.executeJsScript(MOBILE_SCREEN_HEIGHT, driver);
            long windowHeight = (Long) JavascriptUtil.executeJsScript(WINDOW_INNER_HEIGHT, driver);
            return (int) (screenHeight - windowHeight);
        }
        return 0;
    }

    private boolean isNativeOrIosMobilebrowser(final Capabilities deviceCapabilities) {
        return UiType.NATIVE.equals(dependencies.getUiType())
                || isIosMobilebrowser(deviceCapabilities.getPlatformName().name());
    }

    private boolean isIosMobilebrowser(final String platformName) {
        return UiType.MOBILE_BROWSER.equals(dependencies.getUiType())
                && platformName.equalsIgnoreCase(Platform.MAC.name());
    }
}
