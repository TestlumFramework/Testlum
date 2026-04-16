package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NativeCompareImageExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private ImageComparator imageComparator;
    @Mock
    private ImageComparisonUtil imageComparisonUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private ApplicationContext context;

    private NativeCompareImageExecutor executor;
    private RemoteWebDriver driver;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        driver = mock(RemoteWebDriver.class);
        scenarioFile = mock(File.class);
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(context.getBean(ImageComparator.class)).thenReturn(imageComparator);
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();
        executor = new NativeCompareImageExecutor(deps);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
    }

    @Nested
    class Annotation {

        @Test
        void hasExecutorForClassAnnotationForNativeImage() {
            ExecutorForClass annotation = NativeCompareImageExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(NativeImage.class, annotation.value());
        }
    }

    @Nested
    class Initialization {

        @Test
        void createsExecutor() {
            assertNotNull(executor);
        }

        @Test
        void hasImageComparatorField() {
            Object field = ReflectionTestUtils.getField(executor, "imageComparator");
            assertNotNull(field);
        }
    }

    @Nested
    class ClassStructure {

        @Test
        void extendsAbstractUiExecutor() {
            assertTrue(com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor.class
                    .isAssignableFrom(NativeCompareImageExecutor.class));
        }
    }

    @Nested
    class CutStatusBar {

        @Test
        void returnsOriginalImageWhenFullScreenIsNull() {
            BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = executor.cutStatusBar(null, image, driver);
            assertEquals(image, result);
        }

        @Test
        void cutsStatusBarForAndroidDeviceWhenFullScreenIsNotNull() {
            FullScreen fullScreen = new FullScreen();
            BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);

            Capabilities capabilities = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(capabilities);
            when(capabilities.getPlatformName()).thenReturn(Platform.ANDROID);
            when(imageComparisonUtil.getStatusBarHeight(driver)).thenReturn(50);

            BufferedImage result = executor.cutStatusBar(fullScreen, image, driver);

            assertNotNull(result);
            assertEquals(150, result.getHeight());
            assertEquals(100, result.getWidth());
        }

        @Test
        void cutsStatusBarForIosDeviceUsingPageSource() {
            FullScreen fullScreen = new FullScreen();
            BufferedImage image = new BufferedImage(200, 400, BufferedImage.TYPE_INT_RGB);

            Capabilities capabilities = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(capabilities);
            when(capabilities.getPlatformName()).thenReturn(Platform.MAC);

            // Provide a page source containing the XCUIElementTypeNavigationBar line
            String pageSource = "<XCUIElementTypeNavigationBar y=\"40\" x=\"0\" width=\"200\" height=\"44\"/>";
            when(driver.getPageSource()).thenReturn(pageSource);

            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(driver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            when(window.getSize()).thenReturn(new Dimension(200, 400));

            BufferedImage result = executor.cutStatusBar(fullScreen, image, driver);

            assertNotNull(result);
            assertEquals(360, result.getHeight()); // 400 - 40
        }

        @Test
        void returnsZeroStatusBarHeightWhenNoNavigationBarFoundOnIos() {
            FullScreen fullScreen = new FullScreen();
            BufferedImage image = new BufferedImage(200, 400, BufferedImage.TYPE_INT_RGB);

            Capabilities capabilities = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(capabilities);
            when(capabilities.getPlatformName()).thenReturn(Platform.MAC);

            String pageSource = "<SomeOtherElement/>";
            when(driver.getPageSource()).thenReturn(pageSource);

            BufferedImage result = executor.cutStatusBar(fullScreen, image, driver);

            assertNotNull(result);
            assertEquals(400, result.getHeight()); // No cutting since y=0
        }

        @Test
        void scalesStatusBarHeightWhenScreenshotIsLargerThanWindowSize() {
            FullScreen fullScreen = new FullScreen();
            // Screenshot at 2x resolution
            BufferedImage image = new BufferedImage(400, 800, BufferedImage.TYPE_INT_RGB);

            Capabilities capabilities = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(capabilities);
            when(capabilities.getPlatformName()).thenReturn(Platform.MAC);

            String pageSource = "<XCUIElementTypeNavigationBar y=\"40\" x=\"0\" width=\"200\" height=\"44\"/>";
            when(driver.getPageSource()).thenReturn(pageSource);

            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(driver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            // Window at 1x
            when(window.getSize()).thenReturn(new Dimension(200, 400));

            BufferedImage result = executor.cutStatusBar(fullScreen, image, driver);

            assertNotNull(result);
            // statusBarHeight = 40, scaling = 800/400 = 2, so scaled = 80
            assertEquals(720, result.getHeight()); // 800 - 80
        }
    }

    @Nested
    class Execute {

        @Test
        void logsAndAddsMetadataOnExecute() {
            NativeImage image = new NativeImage();
            image.setFile("expected.png");
            CommandResult result = new CommandResult();

            File expectedFile = mock(File.class);
            when(fileSearcher.searchFileFromDir(any(), any())).thenReturn(expectedFile);

            try {
                executor.execute(image, result);
            } catch (Exception ignored) {
                // Expected exception - test verifies logging behavior
            }

            verify(logUtil).logImageComparisonInfo(image);
            verify(resultUtil).addImageComparisonMetaData(image, result);
        }

        @Test
        void wrapsIOExceptionInDefaultFrameworkException() {
            NativeImage image = new NativeImage();
            image.setFile("nonexistent.png");

            when(fileSearcher.searchFileFromDir(any(), eq("nonexistent.png")))
                    .thenReturn(new File("/nonexistent/path.png"));

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(image, result));
        }

        @Test
        void fullScreenComparisonWithNoStatusBarCut() throws IOException {
            NativeImage image = new NativeImage();
            image.setFile("expected.png");
            // No fullScreen set, so no status bar cutting

            BufferedImage testImage = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            File screenshotFile = File.createTempFile("screenshot", ".png");
            screenshotFile.deleteOnExit();
            ImageIO.write(testImage, "png", screenshotFile);
            when(uiUtil.takeScreenshot((WebDriver) driver)).thenReturn(screenshotFile);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(NativeImage.class), any(BufferedImage.class),
                    any(BufferedImage.class))).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));

            verify(imageComparisonUtil).processImageComparisonResult(eq(comparisonResult),
                    eq("expected.png"), eq(false), any(), eq(result));
        }

        @Test
        void partImageComparisonCropsElementArea() throws IOException {
            NativeImage image = new NativeImage();
            image.setFile("expected.png");
            Part part = new Part();
            part.setLocator("elementLocator");
            image.setPart(part);

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            WebElement mockElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("elementLocator"), any()))
                    .thenReturn(mockElement);
            when(mockElement.getLocation()).thenReturn(new Point(10, 10));
            when(mockElement.getSize()).thenReturn(new Dimension(50, 50));

            // Create a full screenshot that is large enough to contain the element area
            BufferedImage fullScreenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            File screenshotFile = File.createTempFile("fullscreen", ".png");
            screenshotFile.deleteOnExit();
            ImageIO.write(fullScreenshot, "png", screenshotFile);
            when(driver.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(NativeImage.class), any(BufferedImage.class),
                    any(BufferedImage.class))).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));
        }
    }
}
