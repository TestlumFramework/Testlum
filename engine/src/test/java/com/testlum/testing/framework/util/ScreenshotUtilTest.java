package com.testlum.testing.framework.util;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.CommandResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ScreenshotUtilTest {

    @InjectMocks
    private ScreenshotUtil screenshotUtil;

    // ================================================================
    // TakeScreenshot
    // ================================================================

    @Nested
    class TakeScreenshot {

        private interface ScreenshotDriver extends WebDriver, TakesScreenshot {
        }

        @Test
        void takeScreenshotFromDriver() {
            ScreenshotDriver driver = mock(ScreenshotDriver.class);
            File screenshotFile = new File("/tmp/screenshot.png");
            when(driver.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = screenshotUtil.takeScreenshot((WebDriver) driver);
            assertEquals(screenshotFile, result);
        }

        @Test
        void takeScreenshotFromElement() {
            WebElement element = mock(WebElement.class);
            File screenshotFile = new File("/tmp/element-screenshot.png");
            when(element.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = screenshotUtil.takeScreenshot(element);
            assertEquals(screenshotFile, result);
        }
    }

    // ================================================================
    // PutScreenshotToResult
    // ================================================================

    @Nested
    class PutScreenshotToResult {

        /**
         * The compressor used to be called here and its output discarded, so reports have always carried
         * the original bytes. The dead call is gone; this pins the behaviour that was always the real one.
         */
        @Test
        void encodesTheScreenshotFileByteForByte(@TempDir final Path tempDir) throws IOException {
            byte[] bytes = {1, 2, 3, 4, 5};
            File screenshot = tempDir.resolve("shot.png").toFile();
            Files.write(screenshot.toPath(), bytes);
            CommandResult result = new CommandResult();

            screenshotUtil.putScreenshotToResult(result, screenshot);

            assertArrayEquals(bytes, Base64.getDecoder().decode(result.getBase64Screenshot()));
        }

        @Test
        void failsLoudlyWhenTheScreenshotIsNotThere() {
            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class,
                    () -> screenshotUtil.putScreenshotToResult(result, new File("/nowhere/missing.png")));
        }

        @Test
        void encodesTheBaselineAsAReadableImage() throws IOException {
            CommandResult result = new CommandResult();

            screenshotUtil.putBaselineScreenshotToResult(result,
                    new BufferedImage(8, 4, BufferedImage.TYPE_INT_RGB), "png");

            byte[] png = Base64.getDecoder().decode(result.getBase64BaselineScreenshot());
            BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png));
            assertEquals(8, decoded.getWidth());
            assertEquals(4, decoded.getHeight());
        }
    }

}
