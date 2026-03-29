package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.SwipeDirection;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.SwipePage;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwipeNativeExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ApplicationContext context;

    @Nested
    class Execute {
        @Test
        void performsPageSwipeUp() {
            AppiumDriver appiumDriver = mock(AppiumDriver.class);
            when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(context)
                    .driver(appiumDriver)
                    .build();
            SwipeNativeExecutor executor = new SwipeNativeExecutor(deps);
            ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
            ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
            ReflectionTestUtils.setField(executor, "logUtil", logUtil);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.UP);
            page.setPercent(50);
            page.setQuantity(1);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(appiumDriver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            when(window.getSize()).thenReturn(new Dimension(400, 800));
            when(uiUtil.buildSequence(any(Point.class), any(Point.class), anyInt()))
                    .thenReturn(mock(Sequence.class));
            when(appiumDriver.switchTo()).thenReturn(mock(WebDriver.TargetLocator.class));

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }
    }
}
