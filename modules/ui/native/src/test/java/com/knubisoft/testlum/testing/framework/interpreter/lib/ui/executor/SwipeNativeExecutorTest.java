package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.SwipeDirection;
import com.knubisoft.testlum.testing.model.scenario.SwipeElement;
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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

    private SwipeNativeExecutor createExecutor(final AppiumDriver appiumDriver) {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(appiumDriver)
                .build();
        SwipeNativeExecutor executor = new SwipeNativeExecutor(deps);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        return executor;
    }

    private void mockDriverWindow(final AppiumDriver appiumDriver, final Dimension size) {
        WebDriver.Options options = mock(WebDriver.Options.class);
        WebDriver.Window window = mock(WebDriver.Window.class);
        when(appiumDriver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(size);
        when(appiumDriver.switchTo()).thenReturn(mock(WebDriver.TargetLocator.class));
        when(uiUtil.buildSequence(any(Point.class), any(Point.class), anyInt()))
                .thenReturn(mock(Sequence.class));
    }

    @Nested
    class PageSwipe {

        @Test
        void performsPageSwipeUp() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.UP);
            page.setPercent(50);
            page.setQuantity(1);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }

        @Test
        void performsPageSwipeDown() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.DOWN);
            page.setPercent(30);
            page.setQuantity(1);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }

        @Test
        void performsPageSwipeLeft() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.LEFT);
            page.setPercent(50);
            page.setQuantity(1);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }

        @Test
        void performsPageSwipeRight() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.RIGHT);
            page.setPercent(50);
            page.setQuantity(1);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }

        @Test
        void performsMultipleSwipesBasedOnQuantity() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipePage page = new SwipePage();
            page.setDirection(SwipeDirection.UP);
            page.setPercent(50);
            page.setQuantity(3);
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setPage(page);

            when(uiUtil.getCenterPoint(appiumDriver)).thenReturn(new Point(200, 400));
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            executor.execute(swipeNative, result);

            verify(appiumDriver, times(3)).perform(any());
        }
    }

    @Nested
    class ElementSwipe {

        @Test
        void performsElementSwipeUp() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipeElement swipeElement = new SwipeElement();
            swipeElement.setDirection(SwipeDirection.UP);
            swipeElement.setPercent(40);
            swipeElement.setQuantity(1);
            swipeElement.setLocator("scrollable-list");
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setElement(swipeElement);

            WebElement element = mock(WebElement.class);
            when(element.getLocation()).thenReturn(new Point(100, 300));
            when(uiUtil.findWebElement(any(), eq("scrollable-list"), any())).thenReturn(element);
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(swipeNative, result));
            verify(appiumDriver).perform(any());
        }

        @Test
        void performsElementSwipeDown() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipeElement swipeElement = new SwipeElement();
            swipeElement.setDirection(SwipeDirection.DOWN);
            swipeElement.setPercent(60);
            swipeElement.setQuantity(2);
            swipeElement.setLocator("carousel");
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setElement(swipeElement);

            WebElement element = mock(WebElement.class);
            when(element.getLocation()).thenReturn(new Point(150, 200));
            when(uiUtil.findWebElement(any(), eq("carousel"), any())).thenReturn(element);
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            executor.execute(swipeNative, result);

            verify(appiumDriver, times(2)).perform(any());
        }

        @Test
        void addsSwipeMetaData() {
            final AppiumDriver appiumDriver = mock(AppiumDriver.class);

            SwipeElement swipeElement = new SwipeElement();
            swipeElement.setDirection(SwipeDirection.LEFT);
            swipeElement.setPercent(50);
            swipeElement.setQuantity(1);
            swipeElement.setLocator("swipe-el");
            SwipeNative swipeNative = new SwipeNative();
            swipeNative.setElement(swipeElement);

            WebElement element = mock(WebElement.class);
            when(element.getLocation()).thenReturn(new Point(100, 100));
            when(uiUtil.findWebElement(any(), eq("swipe-el"), any())).thenReturn(element);
            mockDriverWindow(appiumDriver, new Dimension(400, 800));

            SwipeNativeExecutor executor = createExecutor(appiumDriver);
            CommandResult result = new CommandResult();
            executor.execute(swipeNative, result);

            verify(resultUtil).addSwipeMetaData(eq(swipeNative), eq(result));
            verify(logUtil).logSwipeNativeInfo(eq(swipeNative));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }
}
