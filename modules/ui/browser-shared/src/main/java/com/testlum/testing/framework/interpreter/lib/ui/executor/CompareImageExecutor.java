package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.testlum.testing.framework.EnvironmentLoader;
import com.testlum.testing.framework.constant.DelimiterConstant;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ImageComparator;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ExecutorForClass(Image.class)
public class CompareImageExecutor extends AbstractUiExecutor<Image> {

    private final EnvironmentLoader currentEnvironmentLoader;
    private final ImageComparator imageComparator;

    public CompareImageExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.currentEnvironmentLoader = dependencies.getContext().getBean(EnvironmentLoader.class);
        this.imageComparator = dependencies.getContext().getBean(ImageComparator.class);
    }

    @Override
    public void execute(final Image image, final CommandResult r) {
        try {
            uiLogUtil.logImageComparisonInfo(image);
            resultUtil.addImageComparisonMetaData(image, r);
            File scenarioFile = dependencies.getFile();
            BufferedImage expected = ImageIO.read(fileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
            BufferedImage actual = getActualImage(dependencies.getDriver(), image, r);
            List<Rectangle> excludeList = getExcludeList(image.getFullScreen(), expected, dependencies.getDriver(), r);
            ImageComparisonResult comparisonResult = imageComparator.compare(image, expected, actual, excludeList);
            imageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                    image.isHighlightDifference(), scenarioFile.getParentFile(), r);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        if (Objects.nonNull(image.getPicture())) {
            return getImageFromPicture(image, result);
        }
        if (Objects.nonNull(image.getPart())) {
            return getImageFromPart(image, result);
        }
        return ImageIO.read(screenshotUtil.takeScreenshot(webDriver));
    }

    private BufferedImage getImageFromPicture(final Image image, final CommandResult result) throws IOException {
        WebElement webElement = uiUtil.findWebElement(dependencies, image.getPicture().getLocator(),
                image.getPicture().getLocatorStrategy(),
                ElementChecks.FOR_POSITIONING, result);
        return extractImageFromElement(webElement, image.getPicture().getAttribute(), result);
    }

    private BufferedImage getImageFromPart(final Image image, final CommandResult result) throws IOException {
        WebElement webElement = uiUtil.findWebElement(dependencies, image.getPart().getLocator(),
                image.getPart().getLocatorStrategy(),
                ElementChecks.FOR_POSITIONING, result);
        return ImageIO.read(screenshotUtil.takeScreenshot(webElement));
    }

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToImage = uiUtil.getElementAttribute(webElement, imageSourceAttribute, dependencies.getDriver());
        if (urlToImage.startsWith(DelimiterConstant.SLASH_SEPARATOR)) {
            try {
                urlToImage = getImageURLStartsWithCurrentPageURL(result, urlToImage);
                return ImageIO.read(new URL(urlToImage));
            } catch (final IIOException e) {
                urlToImage = getImageURLStartsWithBaseURL(result, urlToImage);
                return ImageIO.read(new URL(urlToImage));
            }
        }
        return ImageIO.read(new URL(urlToImage));
    }

    private List<Rectangle> getExcludeList(final WebFullScreen fullScreen,
                                           final BufferedImage expected,
                                           final WebDriver driver,
                                           final CommandResult result) {
        if (Objects.nonNull(fullScreen) && !fullScreen.getExclude().isEmpty()) {
            Scale scale = getScaling(expected, driver);
            return fullScreen.getExclude().stream()
                    .map(element -> getExcludeArea(element, scale, result))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Rectangle getExcludeArea(final Exclude exclude, final Scale scale, final CommandResult result) {
        if (exclude.getByLocator() != null) {
            ByLocator byLocator = exclude.getByLocator();
            return getElementArea(byLocator.getLocator(), scale, byLocator.getLocatorStrategy(), result);
        } else {
            return getAreaByCoordinates(exclude);
        }
    }

    private Scale getScaling(final BufferedImage screen, final WebDriver driver) {
        Dimension windowSize = driver.manage().window().getSize();
        if (screen.getWidth() / windowSize.getWidth() > 1 || screen.getHeight() / windowSize.getHeight() > 1) {
            return new Scale(Math.ceil((double) screen.getWidth() / windowSize.getWidth()),
                    Math.ceil((double) screen.getHeight() / (windowSize.getHeight())));
        }
        return new Scale(1, 1);
    }

    private Rectangle getElementArea(final String locatorId, final Scale scale,
                                     final LocatorStrategy locatorStrategy, final CommandResult result) {
        org.openqa.selenium.Rectangle seleniumRectangle =
                uiUtil.findWebElement(dependencies, locatorId, locatorStrategy,
                        ElementChecks.FOR_POSITIONING, result).getRect();
        double x = seleniumRectangle.getX() * scale.getScaleX();
        double y = seleniumRectangle.getY() * scale.getScaleY();
        double width = (seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scale.getScaleX();
        double height = (seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scale.getScaleY();
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    private Rectangle getAreaByCoordinates(final Exclude exclude) {
        ByArea byArea = exclude.getByArea();
        int minX = byArea.getX().intValue();
        int minY = byArea.getY().intValue();
        int maxX = minX + byArea.getWidth().intValue();
        int maxY = minY + byArea.getHeight().intValue();
        return new Rectangle(minX, minY, maxX, maxY);
    }

    private String getImageURLStartsWithBaseURL(final CommandResult result, final String urlToImage) {
        String baseUrl = currentEnvironmentLoader.getCurrentEnvWebSettings().get().getBaseUrl();
        String imageURL = baseUrl + urlToImage;
        log.info(LogMessage.URL_TO_IMAGE_LOG, imageURL);
        result.put(ResultUtil.URL_TO_ACTUAL_IMAGE, imageURL);
        return imageURL;
    }

    private String getImageURLStartsWithCurrentPageURL(final CommandResult result, final String urlToImage) {
        String basePageUrl = uiUtil.getBasePageURL(dependencies.getDriver().getCurrentUrl());
        String imageURL = basePageUrl + urlToImage;
        log.info(LogMessage.URL_TO_IMAGE_LOG, imageURL);
        result.put(ResultUtil.URL_TO_ACTUAL_IMAGE, imageURL);
        return imageURL;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Scale {
        private final double scaleX;
        private final double scaleY;
    }
}