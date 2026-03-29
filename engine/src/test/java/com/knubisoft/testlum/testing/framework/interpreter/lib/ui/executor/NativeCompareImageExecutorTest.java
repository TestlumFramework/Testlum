package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(context.getBean(ImageComparator.class)).thenReturn(imageComparator);
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new NativeCompareImageExecutor(deps);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "imageComparisonUtil", imageComparisonUtil);
        ReflectionTestUtils.setField(executor, "fileSearcher", fileSearcher);
    }

    @Nested
    class CutStatusBar {
        @Test
        void returnsOriginalImageWhenFullScreenIsNull() {
            BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            BufferedImage result = executor.cutStatusBar(null, image, driver);
            assertEquals(image, result);
        }
    }

    @Nested
    class Initialization {
        @Test
        void createsExecutor() {
            assertNotNull(executor);
        }
    }
}
