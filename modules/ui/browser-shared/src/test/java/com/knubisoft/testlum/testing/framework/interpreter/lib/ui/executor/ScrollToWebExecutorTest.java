package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.ScrollTo;
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
class ScrollToWebExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private ScrollToWebExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new ScrollToWebExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
    }

    @Nested
    class Execute {

        @Test
        void scrollsToElementUsingJavascript() {
            ScrollTo scrollTo = new ScrollTo();
            scrollTo.setLocator("footer-section");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("footer-section"), any())).thenReturn(element);

            executor.execute(scrollTo, result);

            verify(javascriptUtil).executeJsScript(anyString(), eq(driver), eq(element));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals("footer-section", result.getMetadata().get(ResultUtil.SCROLL_LOCATOR));
        }
    }
}
