package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DoubleClick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoubleClickExecutorTest {

    interface InteractiveWebDriver extends WebDriver, Interactive { }

    @Mock
    private UiUtil uiUtil;
    @Mock
    private InteractiveWebDriver driver;
    @Mock
    private ApplicationContext context;

    private DoubleClickExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new DoubleClickExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void performsDoubleClickAndRecordsLocator() {
            DoubleClick click = new DoubleClick();
            click.setLocator("row-item");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("row-item"), any())).thenReturn(element);

            executor.execute(click, result);

            verify(uiUtil).highlightElementIfRequired(anyBoolean(), eq(element), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals("row-item", result.getMetadata().get(ResultUtil.DOUBLE_CLICK_LOCATOR));
        }
    }
}
