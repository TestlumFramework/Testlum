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
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.Image;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
        BufferedImage expectedImage = ImageIO.read(FileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actualImage = getActualImage(dependencies.getDriver(), image, result);
        List<Rectangle> excludedElements = getExcludedElements(image);
        if (nonNull(image.getFullScreen()) || nonNull(image.getElement()) || nonNull(image.getPart())) {
            processCompareWith(image, expectedImage, actualImage, scenarioFile, excludedElements, result);
        } else {
            ImageComparisonUtil.findExpectedInActual(image, expectedImage,
                    actualImage, scenarioFile.getParentFile(), excludedElements, result);
        }
    }

    private void processCompareWith(final Image image,
                                    final BufferedImage expectedImage,
                                    final BufferedImage actualImage,
                                    final File scenarioFile,
                                    final List<Rectangle> excludedElements,
                                    final CommandResult result) {
        ImageComparisonResult comparisonResult =
                ImageComparator.compare(image, expectedImage, actualImage, excludedElements);
        ImageComparisonUtil.processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
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
            return getElementAsImage(webDriver, image.getElement().getLocatorId());
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
        WebElement webElement = UiUtil.findWebElement(dependencies, locatorId);
        org.openqa.selenium.Rectangle rectangle = webElement.getRect();
        BufferedImage fullScreen = ImageIO.read(UiUtil.takeScreenshot(webDriver));
        return fullScreen.getSubimage(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
    }

    private List<Rectangle> getExcludedElements(final Image image) {
        List<Rectangle> excludedElements = new ArrayList<>();
        if (nonNull(image.getFullScreen())) {
            image.getFullScreen().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        } else if (nonNull(image.getPart())) {
            image.getPart().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        } else if (nonNull(image.getFindPart())) {
            image.getFindPart().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        }
        return excludedElements;
    }

    private void addElementToExclude(final List<Rectangle> excludedElements, final Exclude element) {
        WebElement webElement = UiUtil.findWebElement(dependencies, element.getLocatorId());
        org.openqa.selenium.Rectangle seleniumRectangle = webElement.getRect();
        Rectangle rectangle = new Rectangle(seleniumRectangle.getX(), seleniumRectangle.getY(),
                seleniumRectangle.getX() + seleniumRectangle.getWidth(),
                seleniumRectangle.getY() + seleniumRectangle.getHeight());
        excludedElements.add(rectangle);
    }
}
