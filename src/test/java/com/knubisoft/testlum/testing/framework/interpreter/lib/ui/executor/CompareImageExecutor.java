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
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.CompareWithFullScreen;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(Image.class)
public class CompareImageExecutor extends AbstractUiExecutor<Image> {

    private static final String MOBILE_SCREEN_WIDTH = "return screen.width;";
    private static final String MOBILE_SCREEN_HEIGHT = "return screen.height;";
    private static final String STATUS_BAR_HEIGHT = "statBarHeight";

    public CompareImageExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @SneakyThrows
    @Override
    public void execute(final Image image, final CommandResult result) {
        LogUtil.logImageComparisonInfo(image);
        ResultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expected = ImageIO.read(FileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actual = getActualImage(dependencies.getDriver(), image, result);
        List<Rectangle> excludeList = getExcludeList(image.getFullScreen(), expected, dependencies.getDriver());
        ImageComparisonResult comparisonResult = ImageComparator.compare(image, expected, actual, excludeList);
        ImageComparisonUtil.processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        if (nonNull(image.getElement())) {
            WebElement webElement = UiUtil.findWebElement(dependencies, image.getElement().getLocatorId(),
                    image.getElement().getLocatorStrategy());
            if (UiType.NATIVE == dependencies.getUiType()) {
                return ImageIO.read(UiUtil.takeScreenshot(webElement));
            }
            return extractImageFromElement(webElement, image.getElement().getAttribute(), result);
        }
        if (nonNull(image.getPart())) {
            WebElement webElement = UiUtil.findWebElement(dependencies, image.getPart().getLocatorId(),
                    image.getElement().getLocatorStrategy());
            return ImageIO.read(webElement.getScreenshotAs(OutputType.FILE));
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToImage = UiUtil.getElementAttribute(webElement, imageSourceAttribute, dependencies.getDriver());
        if (UiType.MOBILE_BROWSER == dependencies.getUiType()) {
            urlToImage = UiUtil.resolveHostIfNeeded(urlToImage);
        }
        log.info(URL_TO_IMAGE_LOG, urlToImage);
        result.put(URL_TO_ACTUAL_IMAGE, urlToImage);
        return ImageIO.read(new URL(urlToImage));
    }

    private List<Rectangle> getExcludeList(final CompareWithFullScreen fullScreen,
                                           final BufferedImage expected,
                                           final WebDriver driver) {
        if (nonNull(fullScreen) && !fullScreen.getExclude().isEmpty()) {
            Scale scale = getScaling(expected, driver);
            return fullScreen.getExclude().stream()
                    .map(element -> getElementArea(element.getLocatorId(), scale, element.getLocatorStrategy()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Scale getScaling(final BufferedImage screen, final WebDriver driver) {
        if (UiType.MOBILE_BROWSER.equals(dependencies.getUiType())) {
            return getMobilebrowserScale(screen, driver);
        }
        Dimension windowSize = driver.manage().window().getSize();
        if (screen.getWidth() / windowSize.getWidth() > 1 || screen.getHeight() / windowSize.getHeight() > 1) {
            return new Scale(Math.ceil((double) screen.getWidth() / windowSize.getWidth()),
                    Math.ceil((double) screen.getHeight() / (windowSize.getHeight())));
        }
        return new Scale(1, 1);
    }

    private Scale getMobilebrowserScale(final BufferedImage screen, final WebDriver driver) {
        Capabilities capabilities = ((RemoteWebDriver) driver).getCapabilities();
        int statBarHeight = Integer.parseInt(capabilities.getCapability(STATUS_BAR_HEIGHT).toString());
        int width = Integer.parseInt(((JavascriptExecutor) driver).executeScript(MOBILE_SCREEN_WIDTH).toString());
        int height = Integer.parseInt(((JavascriptExecutor) driver).executeScript(MOBILE_SCREEN_HEIGHT).toString());
        if (screen.getWidth() / width > 1 || screen.getHeight() / height > 1) {
            return new Scale((double) screen.getWidth() / width,
                    (double) screen.getHeight() / (height + ((double) statBarHeight / 2)));
        }
        return new Scale(1, 1);
    }

    private Rectangle getElementArea(final String locatorId, final Scale scale, final LocatorStrategy locatorStrategy) {
        org.openqa.selenium.Rectangle seleniumRectangle =
                UiUtil.findWebElement(dependencies, locatorId, locatorStrategy).getRect();
        double x = seleniumRectangle.getX() * scale.getScaleX();
        double y = seleniumRectangle.getY() * scale.getScaleY();
        double width = (seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scale.getScaleX();
        double height = (seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scale.getScaleY();
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    @Getter
    @RequiredArgsConstructor
    private static class Scale {
        private final double scaleX;
        private final double scaleY;
    }
}
