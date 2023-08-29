package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
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
import com.knubisoft.testlum.testing.model.scenario.NativeFullScreen;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.IPHONE_ELEMENT_COMMAND_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.NATIVE_PICTURE_COMMAND_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(NativeImage.class)
public class NativeCompareImageExecutor extends AbstractUiExecutor<NativeImage> {

    private static final String MOBILE_SCREEN_HEIGHT = "return screen.height;";
    private static final String WINDOW_INNER_HEIGHT = "return window.innerHeight;";
    private static final String GET_PLATFORM_SCRIPT = "return navigator.platform";
    private static final String STATUS_BAR_HEIGHT = "statBarHeight";
    private static final String IPHONE = "iPhone";

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
        if (nonNull(actual)) {
            List<Rectangle> excludeList = getExcludeList(image.getFullScreen(), expected, dependencies.getDriver());
            ImageComparisonResult comparisonResult =
                    ImageComparator.compare(image.getFullScreen(), image.getElement(), expected, actual, excludeList);
            ImageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                    image.isHighlightDifference(), scenarioFile.getParentFile(), result);
        }
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final NativeImage image,
                                         final CommandResult result) throws IOException {
        if (nonNull(image.getPicture())) {
            return getElementsPicture(image, result);
        }
        if (nonNull(image.getElement())) {
            return getElementAsScreenshot(webDriver, image);
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }

    private BufferedImage getElementsPicture(final NativeImage image, final CommandResult result) throws IOException {
        if (UiType.NATIVE == dependencies.getUiType()) {
            log.info(NATIVE_PICTURE_COMMAND_NOT_SUPPORTED);
            return null;
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
                                                 final NativeImage image) throws IOException {
        if (UiType.MOBILE_BROWSER.equals(dependencies.getUiType())
                && JavascriptUtil.executeJsScript(GET_PLATFORM_SCRIPT, webDriver).equals(IPHONE)) {
            log.info(IPHONE_ELEMENT_COMMAND_NOT_SUPPORTED);
            return null;
        }
        WebElement webElement = UiUtil.findWebElement(dependencies, image.getElement().getLocatorId());
        return ImageIO.read(webElement.getScreenshotAs(OutputType.FILE));
    }

    private List<Rectangle> getExcludeList(final NativeFullScreen fullScreen,
                                           final BufferedImage expected,
                                           final WebDriver driver) {
        if (nonNull(fullScreen)) {
            List<Rectangle> excludeList = new ArrayList<>();
            ImageComparisonUtil.Scale scale = getScaling(expected, driver);
            isExcludeStatusBar(fullScreen, driver, excludeList, scale);
            if (!fullScreen.getExclude().isEmpty()) {
                excludeList.addAll(fullScreen.getExclude().stream()
                        .map(element -> getElementArea(element.getLocatorId(), scale))
                        .collect(Collectors.toList()));
            }
            return excludeList;
        }
        return Collections.emptyList();
    }

    private ImageComparisonUtil.Scale getScaling(final BufferedImage screen, final WebDriver driver) {
        int screenWidth = driver.manage().window().getSize().getWidth();
        long screenHeight = getScreenHeight(driver);
        if (screen.getWidth() / screenWidth > 1 || screen.getHeight() / screenHeight > 1) {
            return new ImageComparisonUtil.Scale((double) screen.getWidth() / screenWidth,
                    (double) screen.getHeight() / screenHeight);
        }
        return new ImageComparisonUtil.Scale(1, 1);
    }

    private long getScreenHeight(final WebDriver driver) {
        long screenHeight;
        if (UiType.MOBILE_BROWSER.equals(dependencies.getUiType())) {
            screenHeight = (Long) JavascriptUtil.executeJsScript(MOBILE_SCREEN_HEIGHT, driver);
            if (screenHeight > dependencies.getDriver().manage().window().getSize().getHeight()) {
                screenHeight = dependencies.getDriver().manage().window().getSize().getHeight();
            }
        } else {
            screenHeight = dependencies.getDriver().manage().window().getSize().getHeight();
        }
        return screenHeight;
    }

    private void isExcludeStatusBar(final NativeFullScreen fullScreen,
                                    final WebDriver driver,
                                    final List<Rectangle> excludeList,
                                    final ImageComparisonUtil.Scale scale) {
        if (fullScreen.isExcludeStatusBar()) {
            long statBarHeight = getStatBarHeight(driver);
            int screenWidth = driver.manage().window().getSize().getWidth();
            if (scale.getScaleX() > 1) {
                excludeList.add(new Rectangle(0, 0, (int) (screenWidth * scale.getScaleX()),
                        (int) (statBarHeight / scale.getScaleY())));
            } else {
                excludeList.add(new Rectangle(0, 0, screenWidth, (int) statBarHeight));
            }
        }
    }

    private Rectangle getElementArea(final String locatorId, final ImageComparisonUtil.Scale scale) {
        org.openqa.selenium.Rectangle seleniumRectangle = UiUtil.findWebElement(dependencies, locatorId).getRect();
        double x = seleniumRectangle.getX() * scale.getScaleX();
        double y = seleniumRectangle.getY() * scale.getScaleY();
        double width = (seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scale.getScaleX();
        double height = (seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scale.getScaleY();
        long statBarHeight = getStatBarHeight(dependencies.getDriver());
        long screenHeight = getScreenHeight(dependencies.getDriver());
        return getElementRectangle(screenHeight, statBarHeight, (int) x, y, (int) width, height);
    }

    private long getStatBarHeight(final WebDriver driver) {
        Capabilities capabilities = ((RemoteWebDriver) dependencies.getDriver()).getCapabilities();
        if (nonNull(capabilities.getCapability(STATUS_BAR_HEIGHT))) {
            return (Long) capabilities.getCapability(STATUS_BAR_HEIGHT);
        }
        long screenHeight = (Long) JavascriptUtil.executeJsScript(MOBILE_SCREEN_HEIGHT, driver);
        long windowHeight = (Long) JavascriptUtil.executeJsScript(WINDOW_INNER_HEIGHT, driver);
        return screenHeight - windowHeight;
    }

    private Rectangle getElementRectangle(final long screenHeight,
                                          final long statBarHeight,
                                          final int x,
                                          final double y,
                                          final int width,
                                          final double height) {
        if (screenHeight > dependencies.getDriver().manage().window().getSize().getHeight()) {
            int newStatBarHeight = (int) (statBarHeight - ((statBarHeight * 2) + (statBarHeight / 2)));
            return new Rectangle(x, (int) (y + newStatBarHeight), width, (int) (height + newStatBarHeight));
        }
        final int gap = 15;
        return new Rectangle(x, (int) (y + statBarHeight) + gap, width, (int) (height + statBarHeight) + gap);
    }
}
