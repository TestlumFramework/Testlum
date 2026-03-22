package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ImageComparator;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.MobileImage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Slf4j
@ExecutorForClass(MobileImage.class)
public class MobileCompareImageExecutor extends AbstractUiExecutor<MobileImage> {

    private final ImageComparator imageComparator;

    public MobileCompareImageExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.imageComparator = dependencies.getContext().getBean(ImageComparator.class);
    }

    @SneakyThrows
    @Override
    protected void execute(final MobileImage image, final CommandResult result) {
        logUtil.logImageComparisonInfo(image);
        resultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expected = ImageIO.read(fileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actual = getActualImage(dependencies.getDriver(), image, result);
        ImageComparisonResult comparisonResult = imageComparator.compare(image, expected,
                cutStatusBar(image.getFullScreen(), actual, dependencies.getDriver()));
        imageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                image.isHighlightDifference(), scenarioFile.getParentFile(), result);
    }

    //CHECKSTYLE:OFF
    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final MobileImage image,
                                         final CommandResult result) throws IOException {
        if (Objects.nonNull(image.getPicture())) {
            WebElement webElement = uiUtil.findWebElement(dependencies, image.getPicture().getLocator(),
                    image.getPicture().getLocatorStrategy());
            return extractImageFromElement(webElement, image.getPicture().getAttribute(), result);
        }
        if (Objects.nonNull(image.getPart())) {
            if (UiType.MOBILE_BROWSER.equals(dependencies.getUiType()) && isIosDevice(webDriver)) {
                throw new DefaultFrameworkException(ExceptionMessage.IOS_NOT_SUPPORT_PART_COMMAND);
            }
            WebElement webElement = uiUtil.findWebElement(dependencies, image.getPart().getLocator(),
                    image.getPart().getLocatorStrategy());
            return ImageIO.read(uiUtil.takeScreenshot(webElement));
        }
        return ImageIO.read(uiUtil.takeScreenshot(webDriver));
    }
    //CHECKSTYLE:ON

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToImage = uiUtil.getElementAttribute(webElement, imageSourceAttribute, dependencies.getDriver());
        urlToImage = uiUtil.resolveHostIfNeeded(urlToImage);
        log.info(LogMessage.URL_TO_IMAGE_LOG, urlToImage);
        result.put(ResultUtil.URL_TO_ACTUAL_IMAGE, urlToImage);
        return ImageIO.read(new URL(urlToImage));
    }

    private BufferedImage cutStatusBar(final FullScreen fullScreen,
                                       final BufferedImage screenshot,
                                       final WebDriver driver) {
        if (Objects.nonNull(fullScreen) && isIosDevice(driver)) {
            int statusBarHeight = imageComparisonUtil.getStatusBarHeight(driver);
            return screenshot.getSubimage(0, statusBarHeight, screenshot.getWidth(),
                    screenshot.getHeight() - statusBarHeight);
        }
        return screenshot;
    }

    private boolean isIosDevice(final WebDriver driver) {
        Platform platformName = ((RemoteWebDriver) driver).getCapabilities().getPlatformName();
        return platformName.equals(Platform.MAC);
    }
}
