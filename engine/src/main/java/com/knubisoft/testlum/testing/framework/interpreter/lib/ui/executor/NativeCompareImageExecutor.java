package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ImageComparator;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ExecutorForClass(NativeImage.class)
public class NativeCompareImageExecutor extends AbstractUiExecutor<NativeImage> {

    private static final String IOS_NAVIGATION_BAR = "XCUIElementTypeNavigationBar";
    private static final String Y_MIN_POINT = "y=\"(\\d+)\"";

    private final ImageComparator imageComparator;

    public NativeCompareImageExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.imageComparator = dependencies.getContext().getBean(ImageComparator.class);
    }

    @SneakyThrows
    @Override
    protected void execute(final NativeImage image, final CommandResult result) {
        logUtil.logImageComparisonInfo(image);
        resultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expected = ImageIO.read(fileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actual = getActualImage(dependencies.getDriver(), image);
        ImageComparisonResult comparisonResult = imageComparator.compare(image, expected, actual);
        imageComparisonUtil.processImageComparisonResult(comparisonResult, image.getFile(),
                image.isHighlightDifference(), scenarioFile.getParentFile(), result);
    }

    private BufferedImage getActualImage(final WebDriver webDriver,
                                         final NativeImage image) throws IOException {
        if (Objects.nonNull(image.getPart())) {
            WebElement webElement = uiUtil.findWebElement(dependencies, image.getPart().getLocator(),
                    image.getPart().getLocatorStrategy());
            File screenshotFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            BufferedImage fullImage = ImageIO.read(screenshotFile);
            Point point = webElement.getLocation();
            int elementWidth = webElement.getSize().getWidth();
            int elementHeight = webElement.getSize().getHeight();
            return fullImage.getSubimage(point.getX(), point.getY(), elementWidth, elementHeight);
        }
        BufferedImage fullScreen = ImageIO.read(uiUtil.takeScreenshot(webDriver));
        return cutStatusBar(image.getFullScreen(), fullScreen, webDriver);
    }

    public BufferedImage cutStatusBar(final FullScreen fullScreen,
                                      final BufferedImage screenshot,
                                      final WebDriver driver) {
        if (Objects.nonNull(fullScreen)) {
            int statusBarHeight = getStatusBarHeight(screenshot, driver);
            return screenshot.getSubimage(0, statusBarHeight, screenshot.getWidth(),
                    screenshot.getHeight() - statusBarHeight);
        }
        return screenshot;
    }

    private int getStatusBarHeight(final BufferedImage screenshot, final WebDriver driver) {
        Platform platformName = ((RemoteWebDriver) driver).getCapabilities().getPlatformName();
        return platformName.equals(Platform.MAC) || platformName.equals(Platform.IOS)
                ? getIosStatusBarHeight(screenshot, driver) : imageComparisonUtil.getStatusBarHeight(driver);
    }

    private int getIosStatusBarHeight(final BufferedImage screenshot, final WebDriver driver) {
        String s = driver.getPageSource().lines()
                .filter(line -> line.contains(IOS_NAVIGATION_BAR))
                .findFirst().orElse(StringUtils.EMPTY);
        Pattern r = Pattern.compile(Y_MIN_POINT);
        Matcher m = r.matcher(s);
        if (m.find()) {
            String yValueStr = m.group(1);
            return scaleStatusBar(Integer.parseInt(yValueStr), screenshot, driver);
        }
        return 0;
    }

    private int scaleStatusBar(final int statusBarHeight,
                               final BufferedImage screenshot,
                               final WebDriver driver) {
        int screenHeight = driver.manage().window().getSize().getHeight();
        int scaling = screenshot.getHeight() > screenHeight ? screenshot.getHeight() / screenHeight : 1;
        return statusBarHeight * scaling;
    }
}
