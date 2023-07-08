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
import com.knubisoft.testlum.testing.model.scenario.CompareWith;
import com.knubisoft.testlum.testing.model.scenario.CompareWithImage;
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.FindIn;
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
        if (nonNull(image.getCompareWith())) {
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
        if (nonNull(image.getCompareWith())) {
            return getCompareWithActualImage(webDriver, image.getCompareWith(), result);
        }
        if (nonNull(image.getFindIn().getFullScreen())) {
            return ImageIO.read(UiUtil.takeScreenshot(webDriver));
        } else {
            return getElementAsImage(webDriver, image.getFindIn().getElement().getLocatorId());
        }
    }

    private BufferedImage getCompareWithActualImage(final WebDriver webDriver,
                                                    final CompareWith compareWith,
                                                    final CommandResult result) throws IOException {
        CompareWithImage compareWithImage = compareWith.getImage();
        if (nonNull(compareWithImage)) {
            WebElement webElement = UiUtil.findWebElement(dependencies, compareWithImage.getLocatorId());
            if (UiType.NATIVE == dependencies.getUiType()) {
                return ImageIO.read(UiUtil.takeScreenshot(webElement));
            }
            return extractImageFromElement(webElement, compareWithImage.getAttribute(), result);
        } else if (nonNull(compareWith.getElement())) {
            return getElementAsImage(webDriver, compareWith.getElement().getLocatorId());
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

    public List<Rectangle> getExcludedElements(final Image image) {
        List<Rectangle> excludedElements = new ArrayList<>();
        CompareWith compareWith = image.getCompareWith();
        FindIn findIn = image.getFindIn();
        if (nonNull(compareWith) && nonNull(compareWith.getFullScreen())) {
            compareWith.getFullScreen().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        } else if (nonNull(compareWith) && nonNull(compareWith.getElement())) {
            compareWith.getElement().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        } else if (nonNull(findIn) && nonNull(findIn.getFullScreen())) {
            findIn.getFullScreen().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
        } else if (nonNull(findIn) && nonNull(findIn.getElement())) {
            findIn.getElement().getExclude().forEach(element -> addElementToExclude(excludedElements, element));
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
