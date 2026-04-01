package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.Part;
import com.knubisoft.testlum.testing.model.scenario.WebFullScreen;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompareImageExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private ImageComparisonUtil imageComparisonUtil;
    @Mock
    private ImageComparator imageComparator;
    @Mock
    private EnvironmentLoader environmentLoader;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private CompareImageExecutor executor;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();
        executor = new CompareImageExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "imageComparator", imageComparator);
        ReflectionTestUtils.setField(executor, "currentEnvironmentLoader", environmentLoader);
    }

    @Nested
    class Annotation {

        @Test
        void hasExecutorForClassAnnotationForImage() {
            ExecutorForClass annotation = CompareImageExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(Image.class, annotation.value());
        }
    }

    @Nested
    class Initialization {

        @Test
        void createsExecutorSuccessfully() {
            assertNotNull(executor);
        }
    }

    @Nested
    class Execute {

        @Test
        void logsAndAddsMetadata() {
            Image image = new Image();
            image.setFile("expected.png");
            CommandResult result = new CommandResult();

            File expectedFile = mock(File.class);
            when(fileSearcher.searchFileFromDir(any(), any())).thenReturn(expectedFile);

            try {
                executor.execute(image, result);
            } catch (Exception ignored) {
                // Expected since we cannot provide real image files in unit tests
            }

            verify(logUtil).logImageComparisonInfo(image);
            verify(resultUtil).addImageComparisonMetaData(image, result);
        }

        @Test
        void fullScreenComparisonWithNoExcludeList() throws IOException {
            Image image = new Image();
            image.setFile("expected.png");

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(testImage, "png", baos);
            java.io.File screenshotFile = File.createTempFile("screenshot", ".png");
            screenshotFile.deleteOnExit();
            ImageIO.write(testImage, "png", screenshotFile);
            when(uiUtil.takeScreenshot(driver)).thenReturn(screenshotFile);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(Image.class), any(BufferedImage.class),
                    any(BufferedImage.class), anyList())).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));
            verify(imageComparisonUtil).processImageComparisonResult(eq(comparisonResult),
                    eq("expected.png"), eq(false), any(), eq(result));
        }

        @Test
        void partImageComparison() throws IOException {
            Image image = new Image();
            image.setFile("expected.png");
            Part part = new Part();
            part.setLocator("partLocator");
            image.setPart(part);

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            WebElement partElement = mock(WebElement.class);
            when(uiUtil.findWebElement(any(ExecutorDependencies.class), eq("partLocator"), any()))
                    .thenReturn(partElement);

            File partScreenshot = File.createTempFile("part-screenshot", ".png");
            partScreenshot.deleteOnExit();
            ImageIO.write(testImage, "png", partScreenshot);
            when(uiUtil.takeScreenshot(partElement)).thenReturn(partScreenshot);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(Image.class), any(BufferedImage.class),
                    any(BufferedImage.class), anyList())).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));
        }

        @Test
        void wrapsIOExceptionInDefaultFrameworkException() {
            Image image = new Image();
            image.setFile("nonexistent.png");

            when(fileSearcher.searchFileFromDir(any(), eq("nonexistent.png")))
                    .thenReturn(new File("/nonexistent/path.png"));

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(image, result));
        }
    }

    @Nested
    class ExcludeList {

        @Test
        void returnsEmptyListWhenFullScreenIsNull() throws Exception {
            Image image = new Image();
            image.setFile("expected.png");
            // fullScreen is null by default

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            File screenshotFile = File.createTempFile("screenshot", ".png");
            screenshotFile.deleteOnExit();
            ImageIO.write(testImage, "png", screenshotFile);
            when(uiUtil.takeScreenshot(driver)).thenReturn(screenshotFile);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(Image.class), any(BufferedImage.class),
                    any(BufferedImage.class), eq(Collections.emptyList()))).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));

            verify(imageComparator).compare(any(Image.class), any(BufferedImage.class),
                    any(BufferedImage.class), eq(Collections.emptyList()));
        }

        @Test
        void returnsEmptyListWhenFullScreenHasEmptyExcludeList() throws Exception {
            Image image = new Image();
            image.setFile("expected.png");
            WebFullScreen fullScreen = new WebFullScreen();
            image.setFullScreen(fullScreen);

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            File screenshotFile = File.createTempFile("screenshot", ".png");
            screenshotFile.deleteOnExit();
            ImageIO.write(testImage, "png", screenshotFile);
            when(uiUtil.takeScreenshot(driver)).thenReturn(screenshotFile);

            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(imageComparator.compare(any(Image.class), any(BufferedImage.class),
                    any(BufferedImage.class), eq(Collections.emptyList()))).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));
        }
    }

    @Nested
    class ClassStructure {

        @Test
        void extendsAbstractUiExecutor() {
            assertTrue(com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor.class
                    .isAssignableFrom(CompareImageExecutor.class));
        }

        @Test
        void hasImageComparatorField() {
            Object field = ReflectionTestUtils.getField(executor, "imageComparator");
            assertNotNull(field);
        }

        @Test
        void hasEnvironmentLoaderField() {
            Object field = ReflectionTestUtils.getField(executor, "currentEnvironmentLoader");
            assertNotNull(field);
        }
    }
}
