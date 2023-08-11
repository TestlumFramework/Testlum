package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
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
import com.knubisoft.testlum.testing.model.scenario.CompareWithElement;
import com.knubisoft.testlum.testing.model.scenario.Image;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ELEMENT_OUT_OF_BOUNDS;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;
import static java.util.Objects.nonNull;

@Slf4j
@ExecutorForClass(Image.class)
public class CompareImageExecutor extends AbstractUiExecutor<Image> {

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
        List<Rectangle> excludeList = getExcludeList(image, expected);
        ImageComparisonResult comparisonResult = ImageComparator.compare(image, expected, actual, excludeList);
        ImageComparisonUtil.processComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
//            ImageComparisonUtil.findExpectedInActual(image, expected, actual,
//                    scenarioFile.getParentFile(), excludeList, result);
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        if (nonNull(image.getElement())) {
            return getImageElement(image.getElement(), result);
        }
        if (nonNull(image.getPart())) {
            return getImagePart(webDriver, image);
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }

    private BufferedImage getImagePart(final WebDriver webDriver, final Image image) throws IOException {
        if (UiType.NATIVE == dependencies.getUiType() || UiType.MOBILE_BROWSER == dependencies.getUiType()) {
            return getMobileOrNativeElementAsImage(webDriver, image.getPart().getLocatorId());
        }
        try {
            return getElementAsImage(webDriver, image.getPart().getLocatorId());
        } catch (RasterFormatException e) {
            throw new DefaultFrameworkException(ELEMENT_OUT_OF_BOUNDS);
        }
    }

    private BufferedImage getImageElement(final CompareWithElement compareWithElement,
                                          final CommandResult result) throws IOException {
        WebElement webElement = UiUtil.findWebElement(dependencies, compareWithElement.getLocatorId());
        if (UiType.NATIVE == dependencies.getUiType()) {
            return ImageIO.read(UiUtil.takeScreenshot(webElement));
        }
        return extractImageFromElement(webElement, compareWithElement.getAttribute(), result);
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

    private BufferedImage getMobileOrNativeElementAsImage(final WebDriver webDriver,
                                                          final String locatorId) throws IOException {
        WebElement webElement = UiUtil.findWebElement(dependencies, locatorId);
        org.openqa.selenium.Rectangle rectangle = webElement.getRect();
        BufferedImage fullscreen = ImageIO.read(UiUtil.takeScreenshot(webDriver));
        return fullscreen.getSubimage(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private BufferedImage getElementAsImage(final WebDriver webDriver, final String locatorId) throws IOException {
        BufferedImage fullScreen = ImageIO.read(UiUtil.takeScreenshot(webDriver));
        double scaleX = 0;
        double scaleY = 0;
        Dimension windowSize = dependencies.getDriver().manage().window().getSize();
        if (fullScreen.getWidth() > windowSize.getWidth() || fullScreen.getHeight() > windowSize.getHeight()) {
            scaleX = (double) fullScreen.getWidth() / windowSize.getWidth();
            scaleY = (double) fullScreen.getHeight() / windowSize.getHeight();
        }
        Rectangle rectangle = getElementArea(locatorId, (int) Math.ceil(scaleX), (int) Math.ceil(scaleY));
        return fullScreen.getSubimage((int) rectangle.getMinPoint().getX(), (int) rectangle.getMinPoint().getY(),
                Math.abs(rectangle.getWidth()), Math.abs(rectangle.getHeight()));
    }

    private List<Rectangle> getExcludeList(final Image image, final BufferedImage expected) {
        double scaleX = 0;
        double scaleY = 0;
        Dimension windowSize = dependencies.getDriver().manage().window().getSize();
        if (expected.getWidth() > windowSize.getWidth() || expected.getHeight() > windowSize.getHeight()) {
            scaleX = (double) expected.getWidth() / windowSize.getWidth();
            scaleY = (double) expected.getHeight() / windowSize.getHeight();
        }
        return getExcludedElements(image, (int) Math.ceil(scaleX), (int) Math.ceil(scaleY));
    }

    private List<Rectangle> getExcludedElements(final Image image, final int scaleX, final int scaleY) {
        List<Rectangle> excludeList = new ArrayList<>();
        if (nonNull(image.getFullScreen())) {
            image.getFullScreen().getExclude().forEach(element ->
                    excludeList.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
        } else if (nonNull(image.getPart())) {
            image.getPart().getExclude().forEach(element ->
                    excludeList.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
        }
//        else if (nonNull(image.getFindPart())) {
//            image.getFindPart().getExclude().forEach(element ->
//                    excludeList.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
//        }
        return excludeList;
    }

    private Rectangle getElementArea(final String locatorId, final int scaleX, final int scaleY) {
        org.openqa.selenium.Rectangle seleniumRectangle = UiUtil.findWebElement(dependencies, locatorId).getRect();
        if (scaleX != 0 && scaleY != 0) {
            return new Rectangle(Math.abs(seleniumRectangle.getX() * scaleX),
                    Math.abs(seleniumRectangle.getY() * scaleY),
                    Math.abs((seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scaleX),
                    Math.abs((seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scaleY));
        } else {
            return new Rectangle(Math.abs(seleniumRectangle.getX()), Math.abs(seleniumRectangle.getY()),
                    Math.abs(seleniumRectangle.getX() + seleniumRectangle.getWidth()),
                    Math.abs(seleniumRectangle.getY() + seleniumRectangle.getHeight()));
        }
    }
}
