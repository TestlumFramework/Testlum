package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.InnerScrollScript;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScrollWebExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private InnerScrollScript innerScrollScript;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private ScrollWebExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new ScrollWebExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
        ReflectionTestUtils.setField(executor, "innerScrollScript", innerScrollScript);
    }

    @Nested
    class PageScroll {

        @Test
        void executesPageScrollScript() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.PAGE);
            scroll.setValue(300);
            CommandResult result = new CommandResult();

            executor.execute(scroll, result);

            verify(javascriptUtil).executeJsScript(anyString(), eq(driver));
            verify(resultUtil).addScrollMetaData(eq(scroll), eq(result));
            verify(logUtil).logScrollInfo(eq(scroll));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }

        @Test
        void takesScreenshotAfterPageScroll() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.PAGE);
            scroll.setValue(500);
            CommandResult result = new CommandResult();

            executor.execute(scroll, result);

            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }

    @Nested
    class InnerScroll {

        @Test
        void executesInnerScrollScripts() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.INNER);
            scroll.setValue(200);
            CommandResult result = new CommandResult();
            when(innerScrollScript.getInnerScrollScript(eq(scroll), eq(uiUtil)))
                    .thenReturn(List.of("document.querySelector('.container').scrollTop += 200"));

            executor.execute(scroll, result);

            verify(javascriptUtil).executeJsScript(anyString(), eq(driver));
        }

        @Test
        void throwsWhenAllInnerScrollScriptsFail() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.INNER);
            scroll.setValue(200);
            CommandResult result = new CommandResult();
            when(innerScrollScript.getInnerScrollScript(eq(scroll), eq(uiUtil)))
                    .thenReturn(List.of("failing-script"));
            when(javascriptUtil.executeJsScript(eq("failing-script"), eq(driver)))
                    .thenThrow(new DefaultFrameworkException("error"));

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(scroll, result));
        }

        @Test
        void succeedsIfAtLeastOneScriptWorks() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.INNER);
            scroll.setValue(100);
            when(innerScrollScript.getInnerScrollScript(eq(scroll), eq(uiUtil)))
                    .thenReturn(List.of("failing-script", "working-script"));
            when(javascriptUtil.executeJsScript(eq("failing-script"), eq(driver)))
                    .thenThrow(new DefaultFrameworkException("error"));
            when(javascriptUtil.executeJsScript(eq("working-script"), eq(driver)))
                    .thenReturn(null);

            CommandResult result = new CommandResult();
            executor.execute(scroll, result);

            verify(javascriptUtil, times(2)).executeJsScript(anyString(), eq(driver));
        }

        @Test
        void addsMetadataForInnerScroll() {
            Scroll scroll = new Scroll();
            scroll.setType(ScrollType.INNER);
            scroll.setValue(150);
            CommandResult result = new CommandResult();
            when(innerScrollScript.getInnerScrollScript(eq(scroll), eq(uiUtil)))
                    .thenReturn(List.of("script1"));

            executor.execute(scroll, result);

            verify(resultUtil).addScrollMetaData(eq(scroll), eq(result));
            verify(logUtil).logScrollInfo(eq(scroll));
        }
    }
}
