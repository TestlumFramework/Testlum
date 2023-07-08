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
import com.knubisoft.testlum.testing.model.scenario.CompareWithImage;
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
        ImageComparisonResult comparisonResult =
                ImageComparator.compare(expectedImage, actualImage, excludedElements, image.getCompareWithFullScreen());
        ImageComparisonUtil
                .processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
    }

    private List<Rectangle> getExcludedElements(final Image image) {
        List<Rectangle> excludedElements = new ArrayList<>();
        if (nonNull(image.getCompareWithFullScreen()) && !image.getCompareWithFullScreen().getExclude().isEmpty()) {
            image.getCompareWithFullScreen().getExclude().forEach(element -> {
                WebElement webElement = UiUtil.findWebElement(dependencies, element.getLocatorId());
                org.openqa.selenium.Rectangle seleniumRectangle = webElement.getRect();
                Rectangle rectangle = new Rectangle(seleniumRectangle.getX(), seleniumRectangle.getY(),
                        seleniumRectangle.getX() + seleniumRectangle.getWidth(),
                        seleniumRectangle.getY() + seleniumRectangle.getHeight());
                excludedElements.add(rectangle);
            });
        }
        return excludedElements;
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        CompareWithImage compareWithImage = image.getCompareWithImage();
        if (nonNull(compareWithImage)) {
            WebElement webElement = UiUtil.findWebElement(dependencies, compareWithImage.getLocatorId());
            if (UiType.NATIVE == dependencies.getUiType()) {
                return ImageIO.read(UiUtil.takeScreenshot(webElement));
            }
            return extractImageFromElement(webElement, compareWithImage.getAttribute(), result);
        } else if (nonNull(image.getCompareWithElement())) {
            WebElement webElement = UiUtil.findWebElement(dependencies, image.getCompareWithElement().getLocatorId());
            return getElementAsImage(webDriver, webElement);
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

    private BufferedImage getElementAsImage(final WebDriver webDriver, final WebElement webElement) throws IOException {
        org.openqa.selenium.Rectangle seleniumRectangle = webElement.getRect();
        BufferedImage fullScreen = ImageIO.read(UiUtil.takeScreenshot(webDriver));
        return fullScreen.getSubimage(seleniumRectangle.getX(), seleniumRectangle.getY(),
                seleniumRectangle.getWidth(), seleniumRectangle.getHeight());
    }
}
