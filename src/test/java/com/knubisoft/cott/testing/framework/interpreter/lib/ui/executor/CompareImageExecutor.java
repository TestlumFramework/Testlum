package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.ImageComparator;
import com.knubisoft.cott.testing.framework.util.ImageComparisonUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.CompareWith;
import com.knubisoft.cott.testing.model.scenario.Image;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.URL_TO_IMAGE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.URL_TO_ACTUAL_IMAGE;

@Slf4j
@ExecutorForClass(Image.class)
public class CompareImageExecutor extends AbstractUiExecutor<Image> {

    public static final String APPIUM_LOCALHOST_ALIAS = "10\\.0\\.2\\.2";
    public static final String MOBILE_WEB_SCREENSHOT = "mobileImage.screenshot";

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
        ImageComparisonResult comparisonResult = ImageComparator.compare(expectedImage, actualImage);
        ImageComparisonUtil
                .processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final Image image,
                                         final CommandResult result) throws IOException {
        CompareWith compareWith = image.getCompareWith();
        if (Objects.nonNull(compareWith)) {
            WebElement webElement = UiUtil.findWebElement(webDriver, compareWith.getLocator());
            if (compareWith.getLocator().equals(MOBILE_WEB_SCREENSHOT)) {
                return extractImageFromElement(webElement, compareWith.getAttribute(), result);
            }
            return ImageIO.read(UiUtil.takeScreenshot(webElement));
        }
        return ImageIO.read(UiUtil.takeScreenshot(webDriver));
    }

    private BufferedImage extractImageFromElement(final WebElement webElement,
                                                  final String imageSourceAttribute,
                                                  final CommandResult result) throws IOException {
        String urlToActualImage = UiUtil.getElementAttribute(webElement, imageSourceAttribute)
                .replaceAll(APPIUM_LOCALHOST_ALIAS, "localhost");
        log.info(URL_TO_IMAGE_LOG, urlToActualImage);
        result.put(URL_TO_ACTUAL_IMAGE, urlToActualImage);
        return ImageIO.read(new URL(urlToActualImage));
    }
}
