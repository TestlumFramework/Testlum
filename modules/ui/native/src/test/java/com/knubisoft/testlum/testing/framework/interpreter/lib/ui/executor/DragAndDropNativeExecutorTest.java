package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DragAndDropNativeExecutorTest {

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
        void performsDragAndDrop() {
            AppiumDriver driver = mock(AppiumDriver.class);
            when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(context)
                    .driver(driver)
                    .build();
            DragAndDropNativeExecutor executor = new DragAndDropNativeExecutor(deps);
            ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
            ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
            ReflectionTestUtils.setField(executor, "logUtil", logUtil);

            DragAndDropNative dnd = new DragAndDropNative();
            dnd.setFromLocator("sourceLocator");
            dnd.setFromLocatorStrategy(LocatorStrategy.LOCATOR_ID);
            dnd.setToLocator("targetLocator");
            dnd.setToLocatorStrategy(LocatorStrategy.LOCATOR_ID);

            WebElement sourceElement = mock(WebElement.class);
            WebElement targetElement = mock(WebElement.class);
            when(sourceElement.getLocation()).thenReturn(new Point(10, 20));
            when(targetElement.getLocation()).thenReturn(new Point(100, 200));
            when(uiUtil.findWebElement(eq(deps), eq("sourceLocator"), any())).thenReturn(sourceElement);
            when(uiUtil.findWebElement(eq(deps), eq("targetLocator"), any())).thenReturn(targetElement);
            when(uiUtil.buildSequence(any(Point.class), any(Point.class), anyInt()))
                    .thenReturn(mock(Sequence.class));

            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(dnd, result));
            verify(driver).perform(any());
        }
    }
}
