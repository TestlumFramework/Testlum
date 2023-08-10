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
import com.knubisoft.testlum.testing.model.scenario.CompareWithElement;
import com.knubisoft.testlum.testing.model.scenario.Image;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        List<Rectangle> excludedElements = getExcludeList(image, expected);
//        if (nonNull(image.getFullScreen()) || nonNull(image.getElement()) || nonNull(image.getPart())) {
            ImageComparisonResult comparisonResult = ImageComparator.compare(image, expected, actual, excludedElements);
            ImageComparisonUtil.processComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
//        }
//        else {
//            ImageComparisonUtil.findExpectedInActual(image, expected, actual,
//                    scenarioFile.getParentFile(), excludedElements, result);
//        }
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        CompareWithElement compareWithElement = image.getElement();
        if (nonNull(compareWithElement)) {
            WebElement webElement = UiUtil.findWebElement(dependencies, compareWithElement.getLocatorId());
            if (UiType.NATIVE == dependencies.getUiType()) {
                return ImageIO.read(UiUtil.takeScreenshot(webElement));
            }
            return extractImageFromElement(webElement, compareWithElement.getAttribute(), result);
        } else if (nonNull(image.getPart())) {
            return getElementAsImage(webDriver, image.getPart().getLocatorId());
        } else {
            return ImageIO.read(UiUtil.takeScreenshot(webDriver));
        }
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

    private BufferedImage getElementAsImage(final WebDriver webDriver, final String locatorId) throws IOException {
        BufferedImage fullScreen = ImageIO.read(UiUtil.takeScreenshot(webDriver));
        double scaleX = 0;
        double scaleY = 0;
        Dimension windowSize = dependencies.getDriver().manage().window().getSize();
        if (fullScreen.getWidth() > windowSize.getWidth() || fullScreen.getHeight() > windowSize.getHeight()) {
            scaleX = (double) fullScreen.getWidth() / windowSize.getWidth();
            scaleY = (double) fullScreen.getHeight() / windowSize.getHeight();
        }
        Rectangle rectangle = getElementArea(locatorId, Math.ceil(scaleX), Math.ceil(scaleY));
        return fullScreen.getSubimage((int) rectangle.getMinPoint().getX(), (int) rectangle.getMinPoint().getY(),
                rectangle.getWidth(), rectangle.getHeight());
    }

    private List<Rectangle> getExcludeList(final Image image, final BufferedImage expected) {
        double scaleX = 0;
        double scaleY = 0;
        Dimension windowSize = dependencies.getDriver().manage().window().getSize();
        if (expected.getWidth() > windowSize.getWidth() || expected.getHeight() > windowSize.getHeight()) {
            scaleX = (double) expected.getWidth() / windowSize.getWidth();
            scaleY = (double) expected.getHeight() / windowSize.getHeight();
        }
        return getExcludedElements(image, Math.ceil(scaleX), Math.ceil(scaleY));
    }

    private List<Rectangle> getExcludedElements(final Image image, final double scaleX, final double scaleY) {
        List<Rectangle> excludedElements = new ArrayList<>();
        if (nonNull(image.getFullScreen())) {
            image.getFullScreen().getExclude().forEach(element ->
                    excludedElements.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
        } else if (nonNull(image.getPart())) {
            image.getPart().getExclude().forEach(element ->
                    excludedElements.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
        }
//        else if (nonNull(image.getFindPart())) {
//            image.getFindPart().getExclude().forEach(element ->
//                    excludedElements.add(getElementArea(element.getLocatorId(), scaleX, scaleY)));
//        }
        return excludedElements;
    }

    private Rectangle getElementArea(final String locatorId, final double scaleX, final double scaleY) {
        org.openqa.selenium.Rectangle seleniumRectangle = UiUtil.findWebElement(dependencies, locatorId).getRect();
        if (scaleX != 0 && scaleY != 0) {
            return new Rectangle((int) Math.ceil(seleniumRectangle.getX() * scaleX),
                    (int) Math.ceil(seleniumRectangle.getY() * scaleY),
                    (int) Math.ceil((seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scaleX),
                    (int) Math.ceil((seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scaleY));
        } else {
            return new Rectangle(seleniumRectangle.getX(), seleniumRectangle.getY(),
                    seleniumRectangle.getX() + seleniumRectangle.getWidth(),
                    seleniumRectangle.getY() + seleniumRectangle.getHeight());
        }
    }
}
