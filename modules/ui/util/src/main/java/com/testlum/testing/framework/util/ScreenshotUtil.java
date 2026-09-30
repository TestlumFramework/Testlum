package com.testlum.testing.framework.util;

import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScreenshotUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");
    private final ConfigProvider configProvider;

    public void takeScreenshotAndSaveIfRequired(final CommandResult result, final ExecutorDependencies dependencies) {
        boolean isTakeScreenshots = dependencies.getUiType().getSettings(dependencies.getEnvironment(), configProvider)
                .getTakeScreenshots().isEnabled();
        if (isTakeScreenshots) {
            File screenshot = takeScreenshot(dependencies.getDriver());
            File screenshotsFolder = new File(dependencies.getFile().getParent(),
                    TestResourceSettings.SCREENSHOT_FOLDER);
            tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
            putScreenshotToResult(result, screenshot);
        }
    }

    public File takeScreenshot(final WebDriver webDriver) {
        return ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
    }

    public File takeScreenshot(final WebElement webElement) {
        return webElement.getScreenshotAs(OutputType.FILE);
    }

    public void putScreenshotToResult(final CommandResult result, final File screenshot) {
        try {
            result.setBase64Screenshot(encode(FileUtils.readFileToByteArray(screenshot)));
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public void putBaselineScreenshotToResult(final CommandResult result, final BufferedImage baseline,
                                              final String formatName) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            ImageIO.write(baseline, formatName, bytes);
            result.setBase64BaselineScreenshot(encode(bytes.toByteArray()));
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private static String encode(final byte[] image) {
        return Base64.getEncoder().encodeToString(image);
    }

    private void tryToCopyScreenshotFileToFolder(final File screenshot,
                                                 final File screenshotsFolder,
                                                 final ExecutorDependencies dependencies) {
        try {
            copyScreenshotFileToFolder(screenshot, screenshotsFolder, dependencies);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void copyScreenshotFileToFolder(final File screenshot,
                                            final File screenshotsFolder,
                                            final ExecutorDependencies dependencies) throws IOException {
        LocalTime dateTime = LocalTime.now();
        String screenshotFileName = String.format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                dateTime.format(TIME_FORMATTER),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }
}
