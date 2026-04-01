package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.exception.ImageComparisonException;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ImageComparisonUtilTest {

    @Mock
    private UiUtil uiUtil;

    @Mock
    private ResultUtil resultUtil;

    @Mock
    private JavascriptUtil javascriptUtil;

    @InjectMocks
    private ImageComparisonUtil imageComparisonUtil;

    @Nested
    class ProcessImageComparisonResult {
        @Test
        void doesNothingOnMatch() {
            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(comparisonResult.getImageComparisonState()).thenReturn(ImageComparisonState.MATCH);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> imageComparisonUtil.processImageComparisonResult(
                    comparisonResult, "expected.png", true, new File("/tmp"), result));
            verifyNoInteractions(uiUtil);
        }

        @Test
        void throwsImageComparisonExceptionOnMismatch(@TempDir final Path tempDir) throws IOException {
            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(comparisonResult.getImageComparisonState()).thenReturn(ImageComparisonState.MISMATCH);
            lenient().when(comparisonResult.getDifferencePercent()).thenReturn(15.5f);

            BufferedImage actualImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage resultImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            lenient().when(comparisonResult.getActual()).thenReturn(actualImg);
            lenient().when(comparisonResult.getResult()).thenReturn(resultImg);

            CommandResult result = new CommandResult();

            assertThrows(ImageComparisonException.class,
                    () -> imageComparisonUtil.processImageComparisonResult(
                            comparisonResult, "expected.png", true, tempDir.toFile(), result));
        }

        @Test
        void throwsImageComparisonExceptionOnSizeMismatch(@TempDir final Path tempDir) {
            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(comparisonResult.getImageComparisonState()).thenReturn(ImageComparisonState.SIZE_MISMATCH);

            BufferedImage expectedImg = new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB);
            BufferedImage actualImg = new BufferedImage(100, 150, BufferedImage.TYPE_INT_RGB);
            BufferedImage resultImg = new BufferedImage(200, 300, BufferedImage.TYPE_INT_RGB);
            when(comparisonResult.getExpected()).thenReturn(expectedImg);
            when(comparisonResult.getActual()).thenReturn(actualImg);
            when(comparisonResult.getResult()).thenReturn(resultImg);

            CommandResult result = new CommandResult();

            assertThrows(ImageComparisonException.class,
                    () -> imageComparisonUtil.processImageComparisonResult(
                            comparisonResult, "expected.png", true, tempDir.toFile(), result));

            verify(resultUtil).addImagesSizeMetaData(comparisonResult, result);
        }

        @Test
        void savesActualImageWhenHighlightDifferenceIsFalse(@TempDir final Path tempDir) {
            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(comparisonResult.getImageComparisonState()).thenReturn(ImageComparisonState.MISMATCH);
            when(comparisonResult.getDifferencePercent()).thenReturn(5.0f);

            BufferedImage actualImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            when(comparisonResult.getActual()).thenReturn(actualImg);

            CommandResult result = new CommandResult();

            assertThrows(ImageComparisonException.class,
                    () -> imageComparisonUtil.processImageComparisonResult(
                            comparisonResult, "expected.png", false, tempDir.toFile(), result));
        }
    }

    @Nested
    class GetStatusBarHeight {
        @Test
        void usesCapabilityWhenAvailable() {
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            Capabilities caps = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(caps);
            when(caps.getCapability("statBarHeight")).thenReturn(48L);
            int height = imageComparisonUtil.getStatusBarHeight(driver);
            assertEquals(48, height);
        }

        @Test
        void calculatesFromScreenAndWindowHeight() {
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            Capabilities caps = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(caps);
            when(caps.getCapability("statBarHeight")).thenReturn(null);
            when(javascriptUtil.executeJsScript("return screen.height;", driver)).thenReturn(2400L);
            when(javascriptUtil.executeJsScript("return window.innerHeight;", driver)).thenReturn(2300L);
            int height = imageComparisonUtil.getStatusBarHeight(driver);
            assertEquals(100, height);
        }

        @Test
        void returnsZeroWhenScreenAndWindowHeightEqual() {
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            Capabilities caps = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(caps);
            when(caps.getCapability("statBarHeight")).thenReturn(null);
            when(javascriptUtil.executeJsScript("return screen.height;", driver)).thenReturn(1000L);
            when(javascriptUtil.executeJsScript("return window.innerHeight;", driver)).thenReturn(1000L);
            int height = imageComparisonUtil.getStatusBarHeight(driver);
            assertEquals(0, height);
        }
    }

    @Nested
    class VerifyDirectoryToSave {

        @Test
        void throwsWhenDirectoryIsNull() throws Exception {
            Method method = ImageComparisonUtil.class.getDeclaredMethod("verifyDirectoryToSave", File.class);
            method.setAccessible(true);

            try {
                method.invoke(imageComparisonUtil, (File) null);
                fail("Expected exception");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Null directory causes NullPointerException from getAbsolutePath() call
                assertInstanceOf(NullPointerException.class, e.getCause());
            }
        }

        @Test
        void throwsWhenDirectoryDoesNotExist() throws Exception {
            Method method = ImageComparisonUtil.class.getDeclaredMethod("verifyDirectoryToSave", File.class);
            method.setAccessible(true);

            try {
                method.invoke(imageComparisonUtil, new File("/nonexistent/dir"));
                fail("Expected exception");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertInstanceOf(ImageComparisonException.class, e.getCause());
            }
        }

        @Test
        void throwsWhenPathIsFile(@TempDir final Path tempDir) throws Exception {
            File file = tempDir.resolve("notADir.txt").toFile();
            file.createNewFile();

            Method method = ImageComparisonUtil.class.getDeclaredMethod("verifyDirectoryToSave", File.class);
            method.setAccessible(true);

            try {
                method.invoke(imageComparisonUtil, file);
                fail("Expected exception");
            } catch (java.lang.reflect.InvocationTargetException e) {
                assertInstanceOf(ImageComparisonException.class, e.getCause());
            }
        }

        @Test
        void succeedsWhenDirectoryExists(@TempDir final Path tempDir) throws Exception {
            Method method = ImageComparisonUtil.class.getDeclaredMethod("verifyDirectoryToSave", File.class);
            method.setAccessible(true);

            assertDoesNotThrow(() -> method.invoke(imageComparisonUtil, tempDir.toFile()));
        }
    }

    @Nested
    class GetImageNameToSave {

        @Test
        void generatesCorrectActualImageName() throws Exception {
            Method method = ImageComparisonUtil.class.getDeclaredMethod("getImageNameToSave", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(imageComparisonUtil, "expected_logo.png");

            assertEquals("actual_image_compared_to_expected_logo.png", result);
        }

        @Test
        void handlesJpgExtension() throws Exception {
            Method method = ImageComparisonUtil.class.getDeclaredMethod("getImageNameToSave", String.class);
            method.setAccessible(true);

            String result = (String) method.invoke(imageComparisonUtil, "screenshot.jpg");

            assertEquals("actual_image_compared_to_screenshot.jpg", result);
        }
    }
}
