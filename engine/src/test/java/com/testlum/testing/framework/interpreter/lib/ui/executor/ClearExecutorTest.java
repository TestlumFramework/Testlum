package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.ScreenshotUtil;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.Clear;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ScreenshotUtil screenshotUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private ClearExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new ClearExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void clearsElementAndRecordsLocator() {
            Clear clear = new Clear();
            clear.setLocator("input-field");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("input-field"), any(), eq(ElementChecks.FOR_WRITING), result))
                    .thenReturn(element);

            executor.execute(clear, result);

            verify(uiUtil).highlightElementIfRequired(anyBoolean(), eq(element), eq(driver));
            verify(element).clear();
            verify(screenshotUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals("input-field", result.getMetadata().get(ResultUtil.CLEAR_LOCATOR));
        }
    }
}
