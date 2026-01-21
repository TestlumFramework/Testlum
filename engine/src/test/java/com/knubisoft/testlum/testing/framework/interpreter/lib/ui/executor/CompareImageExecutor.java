package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.ImageComparator;
import com.knubisoft.testlum.testing.framework.util.ImageComparisonUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.WebFullScreen;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import java.util.stream.Collectors;

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
        List<Rectangle> excludeList = getExcludeList(image.getFullScreen(), expected, dependencies.getDriver(), result);
        ImageComparisonResult comparisonResult = ImageComparator.compare(image, expected, actual, excludeList);
        ImageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                image.isHighlightDifference(), scenarioFile.getParentFile(), result);
    }

    //CHECKSTYLE:OFF
    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        if (nonNull(image.getPicture())) {
            WebElement webElement = UiUtil.findWebElement(dependencies, image.getPicture().getLocator(),
                    image.getPicture().getLocatorStrategy(), result);
            return extractImageFromElement(webElement, image.getPicture().getAttribute(), result);
        }
        if (nonNull(image.getPart())) {
            WebElement webElement = UiUtil.findWebElement(dependencies, image.getPart().getLocator(),
                    image.getPart().getLocatorStrategy(), result);
            return ImageIO.read(UiUtil.takeScreenshot(webElement));
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }
    //CHECKSTYLE:ON

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToImage = UiUtil.getElementAttribute(webElement, imageSourceAttribute, dependencies.getDriver());
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
        if (nonNull(fullScreen) && !fullScreen.getExclude().isEmpty()) {
            Scale scale = getScaling(expected, driver);
            return fullScreen.getExclude().stream()
                    .map(element -> getElementArea(element.getLocator(), scale, element.getLocatorStrategy(), result))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Scale getScaling(final BufferedImage screen, final WebDriver driver) {
        Dimension windowSize = driver.manage().window().getSize();
        if (screen.getWidth() / windowSize.getWidth() > 1 || screen.getHeight() / windowSize.getHeight() > 1) {
            return new Scale(Math.ceil((double) screen.getWidth() / windowSize.getWidth()),
                    Math.ceil((double) screen.getHeight() / (windowSize.getHeight())));
        }
        return new Scale(1, 1);
    }

    private Rectangle getElementArea(final String locatorId, final Scale scale, final LocatorStrategy locatorStrategy, final CommandResult result) {
        org.openqa.selenium.Rectangle seleniumRectangle =
                UiUtil.findWebElement(dependencies, locatorId, locatorStrategy, result).getRect();
        double x = seleniumRectangle.getX() * scale.getScaleX();
        double y = seleniumRectangle.getY() * scale.getScaleY();
        double width = (seleniumRectangle.getX() + seleniumRectangle.getWidth()) * scale.getScaleX();
        double height = (seleniumRectangle.getY() + seleniumRectangle.getHeight()) * scale.getScaleY();
        return new Rectangle((int) x, (int) y, (int) width, (int) height);
    }

    private String getImageURLStartsWithBaseURL(final CommandResult result, final String urlToImage) {
        String imageURL;
        String baseUrl = GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv()).getBaseUrl();
        imageURL = baseUrl + urlToImage;
        log.info(URL_TO_IMAGE_LOG, imageURL);
        result.put(URL_TO_ACTUAL_IMAGE, imageURL);
        return imageURL;
    }

    private String getImageURLStartsWithCurrentPageURL(final CommandResult result, final String urlToImage) {
        String imageURL;
        String basePageUrl = UiUtil.getBasePageURL(dependencies.getDriver().getCurrentUrl());
        imageURL = basePageUrl + urlToImage;
        log.info(URL_TO_IMAGE_LOG, imageURL);
        result.put(URL_TO_ACTUAL_IMAGE, imageURL);
        return imageURL;
    }

    @Getter
    @RequiredArgsConstructor
    private static class Scale {
        private final double scaleX;
        private final double scaleY;
    }
}
