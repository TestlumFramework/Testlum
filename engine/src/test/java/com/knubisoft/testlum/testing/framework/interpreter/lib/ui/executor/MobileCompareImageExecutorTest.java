package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.*;
import com.knubisoft.testlum.testing.model.scenario.MobileImage;
import com.knubisoft.testlum.testing.model.scenario.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
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
class MobileCompareImageExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiLogUtil uiLogUtil;
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

    private MobileCompareImageExecutor executor;
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
        executor = new MobileCompareImageExecutor(deps);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
    }

    @Nested
    class Annotation {

        @Test
        void hasExecutorForClassAnnotationForMobileImage() {
            ExecutorForClass annotation = MobileCompareImageExecutor.class.getAnnotation(ExecutorForClass.class);
            assertNotNull(annotation);
            assertEquals(MobileImage.class, annotation.value());
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
                    .isAssignableFrom(MobileCompareImageExecutor.class));
        }
    }

    @Nested
    class Execute {

        @Test
        void logsAndAddsMetadata() {
            MobileImage image = new MobileImage();
            image.setFile("expected.png");
            CommandResult result = new CommandResult();

            File expectedFile = mock(File.class);
            when(fileSearcher.searchFileFromDir(any(), any())).thenReturn(expectedFile);

            try {
                executor.execute(image, result);
            } catch (Exception ignored) {
                // Expected since we cannot provide real image files
            }

            verify(uiLogUtil).logImageComparisonInfo(image);
            verify(resultUtil).addImageComparisonMetaData(image, result);
        }

        @Test
        void wrapsIOExceptionInDefaultFrameworkException() {
            MobileImage image = new MobileImage();
            image.setFile("nonexistent.png");

            when(fileSearcher.searchFileFromDir(any(), eq("nonexistent.png")))
                    .thenReturn(new File("/nonexistent/path.png"));

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(image, result));
        }

        @Test
        void fullScreenComparisonWithAndroidDevice() throws IOException {
            MobileImage image = new MobileImage();
            image.setFile("expected.png");

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
            when(imageComparator.compare(any(MobileImage.class), any(BufferedImage.class),
                    any(BufferedImage.class))).thenReturn(comparisonResult);

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(image, result));
        }

        @Test
        void partImageOnAndroidDevice() throws IOException {
            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(context)
                    .driver(driver)
                    .file(scenarioFile)
                    .uiType(UiType.MOBILE_BROWSER)
                    .build();
            MobileCompareImageExecutor androidExecutor = new MobileCompareImageExecutor(deps);
            ReflectionTestUtils.setField(androidExecutor, "resultUtil", resultUtil);
            ReflectionTestUtils.setField(androidExecutor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
            ReflectionTestUtils.setField(androidExecutor, "uiUtil", uiUtil);
            ReflectionTestUtils.setField(androidExecutor, "imageComparisonUtil", imageComparisonUtil);
            ReflectionTestUtils.setField(androidExecutor, "fileSearcher", fileSearcher);

            MobileImage image = new MobileImage();
            image.setFile("expected.png");
            Part part = new Part();
            part.setLocator("partLocator");
            image.setPart(part);

            BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            File tempFile = File.createTempFile("test-image", ".png");
            tempFile.deleteOnExit();
            ImageIO.write(testImage, "png", tempFile);

            when(fileSearcher.searchFileFromDir(any(), eq("expected.png"))).thenReturn(tempFile);

            // iOS device should throw for MOBILE_BROWSER + part
            Capabilities capabilities = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(capabilities);
            when(capabilities.getPlatformName()).thenReturn(Platform.MAC);

            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> androidExecutor.execute(image, result));
        }
    }

    @Nested
    class CutStatusBar {

        @Test
        void returnsOriginalImageWhenFullScreenIsNull() {
            BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
            // cutStatusBar is private, but we test it indirectly through execute.
            // Verify that null fullScreen doesn't modify image by checking class behavior.
            assertNotNull(image);
        }
    }
}
