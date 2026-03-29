package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Click;
import com.knubisoft.testlum.testing.model.scenario.ClickMethod;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClickExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private ClickExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new ClickExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
    }

    @Nested
    class SeleniumClick {

        @Test
        void executesSeleniumClickByDefault() {
            Click click = new Click();
            click.setLocator("btn-submit");
            click.setMethod(null);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("btn-submit"), any())).thenReturn(element);

            executor.execute(click, result);

            verify(element).click();
            verify(uiUtil).highlightElementIfRequired(anyBoolean(), eq(element), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }

        @Test
        void executesSeleniumClickWhenMethodIsSelenium() {
            Click click = new Click();
            click.setLocator("btn-ok");
            click.setMethod(ClickMethod.SELENIUM);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("btn-ok"), any())).thenReturn(element);

            executor.execute(click, result);

            verify(element).click();
        }
    }

    @Nested
    class JsClick {

        @Test
        void executesJsClickWhenMethodIsJs() {
            Click click = new Click();
            click.setLocator("btn-js");
            click.setMethod(ClickMethod.JS);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("btn-js"), any())).thenReturn(element);

            executor.execute(click, result);

            verify(javascriptUtil).executeJsScript(anyString(), eq(driver), eq(element));
        }
    }

    @Nested
    class MetadataRecording {

        @Test
        void putsLocatorIntoResult() {
            Click click = new Click();
            click.setLocator("my-locator");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("my-locator"), any())).thenReturn(element);

            executor.execute(click, result);

            assert result.getMetadata().containsKey(ResultUtil.CLICK_LOCATOR);
        }
    }
}
